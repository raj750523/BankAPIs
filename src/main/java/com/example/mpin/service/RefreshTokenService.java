package com.example.mpin.service;

import com.example.mpin.constants.LogMessages;
import com.example.mpin.constants.ValidationMessages;
import com.example.mpin.dto.JwtResponse;
import com.example.mpin.dto.LogoutRequest;
import com.example.mpin.dto.RefreshTokenRequest;
import com.example.mpin.model.AppUser;
import com.example.mpin.model.RefreshToken;
import com.example.mpin.repository.AppUserRepository;
import com.example.mpin.repository.RefreshTokenRepository;
import com.example.mpin.security.JwtTokenService;
import com.example.mpin.util.UserServiceUtils;
import com.example.mpin.util.ValidationUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
public class RefreshTokenService {

    private final RefreshTokenRepository repo;
    private final AppUserRepository userRepo;
    private final JwtTokenService jwtTokenService;
    private final ValidationUtil validationUtil;
    private final UserServiceUtils userUtils;

    public RefreshTokenService(RefreshTokenRepository repo, AppUserRepository userRepo,
                               JwtTokenService jwtTokenService,
                               ValidationUtil validationUtil, UserServiceUtils userUtils) {
        this.repo = repo;
        this.userRepo = userRepo;
        this.jwtTokenService = jwtTokenService;
        this.validationUtil = validationUtil;
        this.userUtils = userUtils;
    }

    @Transactional
    public RefreshToken createRefreshToken(Long userId, RefreshTokenRequest request) {

        // Fetch user
        AppUser user = userRepo.findById(userId)
                .orElseThrow(() -> {
                    log.warn(LogMessages.USER_NOT_FOUND, "id=" + userId);
                    return new RuntimeException(ValidationMessages.USER_NOT_FOUND);
                });

        // Delete existing tokens
        repo.deleteByUser(user);
        repo.flush();

        // Validate IP, Device ID, Location

        validationUtil.validateIpFormat(request.getIp(), user.getMobile());
        validationUtil.validateDeviceIdFormat(request.getDeviceId(), user.getMobile());
        validationUtil.validateLocation(request.getLatitude(), String.valueOf(request.getLongitude()), user.getMobile());

        // Create new refresh token
        RefreshToken token = new RefreshToken();
        token.setUser(user);
        token.setToken(UUID.randomUUID().toString());
        token.setExpiryDate(LocalDateTime.now().plusDays(7));

        log.info(LogMessages.REFRESH_TOKEN_CREATED, user.getMobile(), request.getDeviceId(), request.getIp());

        return repo.saveAndFlush(token);
    }

    // ---------------- Refresh access token using existing refresh token ----------------
    @Transactional
    public JwtResponse refreshToken(RefreshTokenRequest request) {

        log.info(LogMessages.REFRESH_TOKEN_REQUEST, request.getDeviceId(), request.getIp());

        // Validate request fields
        if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            log.error(LogMessages.REFRESH_TOKEN_FAILED, "Empty refresh token");
            throw new IllegalArgumentException(ValidationMessages.INVALID_REFRESH_TOKEN);
        }
        userUtils.validateDeviceInfo(request.getIp(), request.getDeviceId(), request.getLatitude(), request.getLongitude(), "refresh-token");


        validationUtil.validateIpFormat(request.getIp(), "refresh-token");
        validationUtil.validateDeviceIdFormat(request.getDeviceId(), "refresh-token");
        validationUtil.validateLocation(request.getLatitude(), String.valueOf(request.getLongitude()), "refresh-token");

        // Verify token exists and not expired
        RefreshToken token = repo.findByToken(request.getRefreshToken())
                .map(this::verifyExpiration)
                .orElseThrow(() -> {
                    log.error(LogMessages.REFRESH_TOKEN_FAILED, "Token not found");
                    return new IllegalArgumentException(ValidationMessages.INVALID_REFRESH_TOKEN);
                });

        // Generate new access token
        String accessToken = jwtTokenService.generateAccessToken(
                java.util.Map.of("mobile", token.getUser().getMobile()),
                token.getUser().getMobile()
        );

        log.info(LogMessages.REFRESH_TOKEN_SUCCESS, token.getUser().getMobile());

        return new JwtResponse(accessToken, request.getRefreshToken());
    }

    // ---------------- Verify refresh token expiration ----------------
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().isBefore(LocalDateTime.now())) {
            repo.delete(token);
            log.warn(LogMessages.REFRESH_TOKEN_EXPIRED, token.getUser().getMobile());
            throw new IllegalArgumentException(ValidationMessages.REFRESH_TOKEN_EXPIRED);
        }
        return token;
    }

    // ---------------- Delete refresh token ----------------
    @Transactional
    public String logout(LogoutRequest request) {
//        log.info(LogMessages.LOGOUT_REQUEST, request.getDeviceId(), request.getIp());

        // ---------- Validations ----------
        if (request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            log.error(LogMessages.LOGOUT_FAILED, "Empty refresh token");
            throw new IllegalArgumentException(ValidationMessages.INVALID_REFRESH_TOKEN);
        }

        userUtils.validateDeviceInfo(request.getIp(), request.getDeviceId(),
                request.getLatitude(), request.getLongitude(), "logout");

        validationUtil.validateIpFormat(request.getIp(), "logout");
        validationUtil.validateDeviceIdFormat(request.getDeviceId(), "logout");

        if (request.getLatitude() != null && request.getLongitude() != null) {
            validationUtil.validateLocation(request.getLatitude(),
                    String.valueOf(request.getLongitude()), "logout");
        }

        // ---------- Mark token inactive ----------
        RefreshToken token = repo.findByToken(request.getRefreshToken())
                .orElseThrow(() -> {
                    log.error(LogMessages.LOGOUT_FAILED, "Token not found");
                    return new IllegalArgumentException(ValidationMessages.INVALID_REFRESH_TOKEN);
                });

        token.setActive(false);
        repo.save(token);

        log.info(LogMessages.LOGOUT_SUCCESS, token.getUser().getMobile());

        return ValidationMessages.LOGOUT_SUCCESS;
    }
}