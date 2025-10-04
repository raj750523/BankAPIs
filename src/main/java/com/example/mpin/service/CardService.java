package com.example.mpin.service;

import com.example.mpin.constants.LogMessages;
import com.example.mpin.constants.ValidationMessages;
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

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CardService {

    private final CardRepository cardRepo;
    private final AppUserRepository userRepo;
    private final ValidationUtil validationUtil;
    private final UserServiceUtils userUtils;


    private static final String DEMO_OTP = "123456";       // demo OTP
    private static final int OTP_TTL_SECONDS = 3600;       // OTP valid for 1 hour in demo
    private static final int MAX_OTP_ATTEMPTS = 5;

    @Transactional
    public void addCard(CardRequest req, String mobile, String ip, String deviceId,
                        Double latitude, Double longitude) {

        log.info(LogMessages.CARD_ADD_REQUEST, mobile, maskPan(req.getCardNumber()));

        // ---------- Basic validations ----------
        if (req.getCardNumber() == null || req.getCardNumber().isBlank()) {
            throw new IllegalArgumentException(ValidationMessages.CARD_ID_BLANK);
        }
        if (!is16Digits(req.getCardNumber())) {
            throw new IllegalArgumentException(ValidationMessages.CARD_ID_INVALID);
        }
        if (req.getHolderName() == null || req.getHolderName().isBlank()) {
            throw new IllegalArgumentException(ValidationMessages.HOLDER_NAME_BLANK);
        }
        if (!req.getHolderName().matches("^[A-Za-z ]{2,50}$")) {
            throw new IllegalArgumentException(ValidationMessages.HOLDER_NAME_INVALID);
        }
        if (req.getValidThru() == null || req.getValidThru().isBlank()) {
            throw new IllegalArgumentException(ValidationMessages.VALID_THRU_BLANK);
        }
        if (!isValidExpiry(req.getValidThru())) {
            throw new IllegalArgumentException(ValidationMessages.VALID_THRU_INVALID);
        }

        // ---------- Device & location validations ----------
        userUtils.validateDeviceInfo(ip, deviceId, latitude, longitude, mobile);
        validationUtil.validateIpFormat(ip, mobile);
        validationUtil.validateDeviceIdFormat(deviceId, mobile);
        if (latitude != null && longitude != null) {
            validationUtil.validateLocation(latitude, String.valueOf(longitude), mobile);
        }

        AppUser user = userUtils.getUserByMobile(mobile);

        // ---------- Duplicate card check ----------
        List<Card> existing = cardRepo.findByCardNumberAndUser(req.getCardNumber(), user);
        if (!existing.isEmpty()) {
            log.warn(LogMessages.CARD_ADD_FAILED, mobile, "duplicate-card");
            throw new IllegalArgumentException(ValidationMessages.CARD_ALREADY_EXISTS);
        }

        // ---------- Save card with demo OTP ----------
        Card card = Card.builder()
                .cardNumber(req.getCardNumber())
                .holderName(req.getHolderName())
                .validThru(req.getValidThru())
                .verified(false)
                .otp(DEMO_OTP)
                .otpExpiry(LocalDateTime.now().plusSeconds(OTP_TTL_SECONDS))
                .otpAttempts(0)
                .user(user)
                .build();

        cardRepo.save(card);

        log.info(LogMessages.CARD_ADD_SUCCESS, mobile, card.getId());
        log.info(LogMessages.OTP_SENT, mobile, maskPan(req.getCardNumber()));
    }

    @Transactional
    public void verifyCardOtp(CardOtpRequest req, String mobile, String ip, String deviceId,
                              Double latitude, Double longitude) {

        log.info(LogMessages.OTP_VERIFY_REQUEST, mobile, maskPan(req.getCardNumber()));

        // ---------- OTP format checks ----------
        if (req.getOtp() == null || req.getOtp().isBlank()) {
            throw new IllegalArgumentException(ValidationMessages.OTP_BLANK);
        }
        if (!req.getOtp().matches("\\d{6}")) {
            throw new IllegalArgumentException(ValidationMessages.OTP_FORMAT_INVALID);
        }

        // ---------- Device & location validations ----------
        userUtils.validateDeviceInfo(ip, deviceId, latitude, longitude, mobile);
        validationUtil.validateIpFormat(ip, mobile);
        validationUtil.validateDeviceIdFormat(deviceId, mobile);
        if (latitude != null && longitude != null) {
            validationUtil.validateLocation(latitude, String.valueOf(longitude), mobile);
        }

        AppUser user = userUtils.getUserByMobile(mobile);

        // ---------- Find card for this user ----------
        List<Card> cards = cardRepo.findByCardNumberAndUser(req.getCardNumber(), user);
        if (cards.isEmpty()) {
            log.warn(LogMessages.OTP_VERIFY_FAILED, mobile, "card-not-found");
            throw new IllegalArgumentException(ValidationMessages.CARD_NOT_FOUND);
        }

        // Prefer unverified card for OTP check
        Card card = cards.stream().filter(c -> !c.isVerified()).findFirst().orElse(cards.get(0));

        // ---------- OTP attempts check ----------
        if (card.getOtpAttempts() >= MAX_OTP_ATTEMPTS) {
            log.warn(LogMessages.OTP_VERIFY_FAILED, mobile, "too-many-attempts");
            throw new IllegalArgumentException(ValidationMessages.TOO_MANY_OTP_ATTEMPTS);
        }

        // ---------- OTP expiry check ----------
        if (card.getOtpExpiry() == null || LocalDateTime.now().isAfter(card.getOtpExpiry())) {
            log.warn(LogMessages.OTP_VERIFY_FAILED, mobile, "otp-expired");
            throw new IllegalArgumentException(ValidationMessages.OTP_INVALID);
        }

        // ---------- OTP verification ----------
        if (!DEMO_OTP.equals(req.getOtp())) {
            card.setOtpAttempts(card.getOtpAttempts() + 1);
            cardRepo.save(card);
            log.warn(LogMessages.OTP_VERIFY_FAILED, mobile, "invalid-otp");
            throw new IllegalArgumentException(ValidationMessages.OTP_INVALID);
        }

        // ---------- Success ----------
        card.setVerified(true);
        card.setOtp(null);
        card.setOtpExpiry(null);
        card.setOtpAttempts(0);
        cardRepo.save(card);

        log.info(LogMessages.OTP_VERIFY_SUCCESS, mobile, maskPan(req.getCardNumber()));
    }

    public List<CardResponse> getCardsForUser(String mobile) {
        AppUser user = userUtils.getUserByMobile(mobile);
        List<Card> cards = cardRepo.findByUser(user);
        log.info(LogMessages.GET_CARDS, mobile, cards.size());
        return cards.stream()
                .map(c -> new CardResponse(c.getId(), c.getHolderName(), maskPan(c.getCardNumber()), c.getValidThru(), c.isVerified()))
                .collect(Collectors.toList());
    }

    public CardResponse getCardById(Long id, String mobile) {
        AppUser user = userUtils.getUserByMobile(mobile);
        Card card = cardRepo.findByIdAndUser(id, user)
                .orElseThrow(() -> new IllegalArgumentException(ValidationMessages.CARD_NOT_FOUND));
        return new CardResponse(card.getId(), card.getHolderName(), maskPan(card.getCardNumber()), card.getValidThru(), card.isVerified());
    }

    // ---------- Helpers ----------
    private boolean is16Digits(String s) {
        return s != null && s.matches("\\d{16}");
    }

    private boolean isValidExpiry(String validThru) {
        try {
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MM/yy");
            YearMonth ym = YearMonth.parse(validThru, fmt);
            return ym.isAfter(YearMonth.now());
        } catch (Exception e) {
            return false;
        }
    }

    private String maskPan(String pan) {
        if (pan == null || pan.length() < 4) return "****";
        return "************" + pan.substring(pan.length() - 4);
    }
}