package com.example.mpin.service;

import com.example.mpin.dto.CardOtpRequest;
import com.example.mpin.dto.CardRequest;
import com.example.mpin.dto.CardResponse;
import com.example.mpin.model.AppUser;
import com.example.mpin.model.Card;
import com.example.mpin.model.CardType;
import com.example.mpin.repository.AppUserRepository;
import com.example.mpin.repository.CardRepository;
import com.example.mpin.util.UserServiceUtils;
import com.example.mpin.util.ValidationUtil;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository repo;
    private final AppUserRepository userRepo;
    //    private final OtpService otpService; // separate OTP service for bank-like flow
    private final ValidationUtil validationUtil;
    private final UserServiceUtils userUtils;


    @Transactional
    public void addCard(CardRequest req, String mobile, String ip, String deviceId, Double latitude, Double longitude) {

        AppUser user = userRepo.findByMobile(mobile)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // ---------- Validate device & location ----------
        userUtils.validateDeviceInfo(ip, deviceId, latitude, longitude, mobile);
        validationUtil.validateIpFormat(ip, mobile);
        validationUtil.validateDeviceIdFormat(deviceId, mobile);
        if (latitude != null && longitude != null) {
            validationUtil.validateLocation(latitude, String.valueOf(longitude), mobile);
        }

        Card card = Card.builder()
                .holderName(req.getHolderName())
                .validThru(req.getValidThru())
                .type(CardType.valueOf(req.getType().toUpperCase()))
                .user(user)
                .otp("123456") // Hardcoded demo OTP
                .verified(false)
                .build();

        repo.save(card);

        // In production: call bank API to send real OTP
        log.info("Card added for {} and OTP sent: {}", mobile, card.getOtp());
    }

    @Transactional
    public void verifyCardOtp(CardOtpRequest req, String mobile, String ip, String deviceId, Double latitude, Double longitude) {

        AppUser user = userRepo.findByMobile(mobile)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        userUtils.validateDeviceInfo(ip, deviceId, latitude, longitude, mobile);

        if (req.getCardId() == null || req.getCardId().isBlank())
            throw new IllegalArgumentException("Card ID cannot be null or empty");

        Long cardId;
        try {
            cardId = Long.parseLong(req.getCardId());
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid Card ID format");
        }

        if (req.getOtp() == null || req.getOtp().isBlank())
            throw new IllegalArgumentException("OTP cannot be null or empty");

        if (!req.getOtp().matches("\\d{6}"))
            throw new IllegalArgumentException("OTP must be 6 digits");

        Card card = repo.findById(cardId)
                .orElseThrow(() -> new IllegalArgumentException("Card not found with ID: " + cardId));

        if (!card.getUser().equals(user))
            throw new IllegalArgumentException("Card does not belong to the logged-in user");

        if (!"123456".equals(req.getOtp()))
            throw new IllegalArgumentException("Invalid OTP");

        card.setVerified(true);
        repo.save(card);

        log.info("Card {} verified successfully for user {}", cardId, mobile);
    }

    public List<CardResponse> getCardsForUser(String mobile) {
        AppUser user = userRepo.findByMobile(mobile)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return repo.findByUser(user).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public CardResponse getCardById(Long id, String mobile) {
        AppUser user = userRepo.findByMobile(mobile)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        Card card = repo.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException("Card not found or not owned by user"));

        return toResponse(card);
    }

    private CardResponse toResponse(Card c) {
        return new CardResponse(
                c.getId(),
                c.getHolderName(),
                c.getCardID (),
                c.getType().name(),
                c.getValidThru(),
                c.isVerified()
        );
    }
}