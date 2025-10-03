package com.example.mpin.service;

import com.example.mpin.bankapi.BankApiClient;
import com.example.mpin.constants.LogMessages;
import com.example.mpin.constants.ValidationMessages;
import com.example.mpin.dto.*;
import com.example.mpin.model.AppUser;
import com.example.mpin.model.UserAudit;
import com.example.mpin.model.RefreshToken;
import com.example.mpin.repository.AppUserRepository;
import com.example.mpin.repository.UserAuditRepository;
import com.example.mpin.security.JwtTokenService;
import com.example.mpin.util.UserServiceUtils;
import com.example.mpin.util.ValidationUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class AuthService {

    private final AppUserRepository userRepo;
    private final UserAuditRepository auditRepo;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final BankApiClient bankApiClient;
    private final RestTemplate restTemplate;
    private final UserServiceUtils userUtils;
    private final ValidationUtil validationUtil;

    private final RefreshTokenService refreshTokenService;


    @Value("${bank.api.base-url:http://localhost:9090/mock-bank}")
    private String bankApiBase;

    public AuthService(AppUserRepository userRepo,
                       UserAuditRepository auditRepo,
                       JwtTokenService jwtTokenService,
                       PasswordEncoder passwordEncoder,
                       ValidationUtil validationUtil,
                       RestTemplate restTemplate, BankApiClient bankApiClient, UserServiceUtils userUtils,
                       RefreshTokenService refreshTokenService, RefreshTokenService refreshTokenService1) {
        this.userRepo = userRepo;
        this.auditRepo = auditRepo;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
        this.refreshTokenService = refreshTokenService1;
        this.bankApiClient = bankApiClient;
        this.userUtils = userUtils;
        this.validationUtil = validationUtil;
    }

    @Transactional
    public String signupStart(SignupStartRequest req) {
        String mobile = req.getMobile().trim();
        log.info(LogMessages.SIGNUP_REQUEST_RECEIVED, mobile);
        userUtils.validateDeviceInfo(req.getIp(), req.getDeviceId(), req.getLatitude(), req.getLongitude(), req.getMobile());

        // Validate mobile
        userUtils.validateMobileNotBlank(mobile);

        String localPattern = "^[6-9][0-9]{9}$";
        if (!mobile.matches(localPattern)) {
            log.warn(LogMessages.MOBILE_INVALID_PATTERN, mobile);
            throw new IllegalArgumentException(ValidationMessages.MOBILE_INVALID_PATTERN);
        }

        // Validate IP, Device ID, and Location
        validationUtil.validateIpFormat(req.getIp(), mobile);
        validationUtil.validateDeviceIdFormat(req.getDeviceId(), mobile);
        validationUtil.validateLocation(req.getLatitude(), String.valueOf(req.getLongitude()), mobile);

        if (userRepo.findByMobile(mobile).isPresent()) {
            log.warn(LogMessages.MOBILE_ALREADY_REGISTERED, mobile);
            throw new IllegalArgumentException(ValidationMessages.MOBILE_ALREADY_REGISTERED);
        }

        AppUser user = new AppUser();
        user.setMobile(mobile);

        if (req.getReferralCode() != null && !req.getReferralCode().isBlank()) {
            user.setReferralCode(req.getReferralCode().trim());
            log.info(LogMessages.REFERRAL_CODE_SAVED, req.getReferralCode(), mobile);
        }

        userRepo.save(user);
        log.info(LogMessages.USER_SAVED, mobile);

        log.info(LogMessages.CALLING_BANK_API, mobile);
        boolean otpSent = bankApiClient.sendOtp(mobile);
        if (!otpSent) {
            log.error(LogMessages.OTP_FAILED, mobile);
            throw new RuntimeException(ValidationMessages.OTP_FAILED);
        }

        log.info(LogMessages.OTP_SUCCESS, mobile);
        return ValidationMessages.OTP_SENT_SUCCESS;
    }

    @Transactional
    public String resendOtp(ResendOtpRequest req) {
        String mobile = req.getMobile().trim();
        userUtils.validateMobileNotBlank(mobile);
        userUtils.validateDeviceInfo(req.getIp(), req.getDeviceId(), req.getLatitude(), req.getLongitude(), req.getMobile());

        AppUser user = userUtils.getUserByMobile(mobile);
        if (user.isOtpVerified()) {
            log.warn(LogMessages.MOBILE_ALREADY_VERIFIED, mobile);
            throw new IllegalArgumentException(ValidationMessages.MOBILE_ALREADY_VERIFIED);
        }

        // Validate IP, Device ID, and Location
        validationUtil.validateIpFormat(req.getIp(), mobile);
        validationUtil.validateDeviceIdFormat(req.getDeviceId(), mobile);
        validationUtil.validateLocation(req.getLatitude(), String.valueOf(req.getLongitude()), mobile);

        String otp = "1234"; // DEV
        user.setOtpHash(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepo.save(user);
        log.info(LogMessages.OTP_RESENT, mobile);
        log.info(LogMessages.OTP_RESENT_SUCCESS, mobile);

        return ValidationMessages.OTP_RESENT_SUCCESS;
    }

    @Transactional
    public String verifyOtp(VerifyOtpRequest req) {
        String mobile = req.getMobile();
        String otp = req.getOtp();

        AppUser user = userUtils.getUserByMobile(mobile);
        userUtils.validateDeviceInfo(req.getIp(), req.getDeviceId(), req.getLatitude(), req.getLongitude(), req.getMobile());
        userUtils.validateOtpNotBlank(otp, mobile);

        // Validate IP, Device ID, and Location
        validationUtil.validateIpFormat(req.getIp(), mobile);
        validationUtil.validateDeviceIdFormat(req.getDeviceId(), mobile);
        validationUtil.validateLocation(req.getLatitude(), String.valueOf(req.getLongitude()), mobile);

        boolean verified = bankApiClient.verifyOtpWithBank(mobile, otp);
        if (!verified) {
            log.warn(LogMessages.OTP_INVALID, mobile);
            throw new IllegalArgumentException(ValidationMessages.OTP_INVALID);
        }

        user.setOtpVerified(true);
        userRepo.save(user);
        log.info(LogMessages.USER_OTP_VERIFIED, mobile);

        log.info(LogMessages.OTP_VERIFIED_SUCCESS, mobile);
        return ValidationMessages.OTP_VERIFIED_SUCCESS;
    }

    @Transactional
    public String setMpin(SetMpinRequest req) {
        String mobile = req.getMobile().trim();
        log.info(LogMessages.SET_MPIN_REQUEST_RECEIVED, mobile);

        userUtils.validateDeviceInfo(req.getIp(), req.getDeviceId(), req.getLatitude(), req.getLongitude(), req.getMobile());

        AppUser user = userUtils.getUserByMobile(mobile);

        // Validate MPIN
        userUtils.validateMpinNotBlank(req.getMpin(), mobile);

        // Validate Confirm MPIN
        userUtils.validateConfirmMpinNotBlank(req.getConfirmMpin(), mobile);

        // Validate IP, Device ID, and Location
        validationUtil.validateIpFormat(req.getIp(), mobile);
        validationUtil.validateDeviceIdFormat(req.getDeviceId(), mobile);
        validationUtil.validateLocation(req.getLatitude(), String.valueOf(req.getLongitude()), mobile);

        if (!req.getMpin().equals(req.getConfirmMpin())) {
            log.warn(LogMessages.MPIN_NOT_MATCH, mobile);
            throw new IllegalArgumentException(ValidationMessages.MPIN_NOT_MATCH);
        }

        user.setMpinHash(passwordEncoder.encode(req.getMpin()));
        userRepo.save(user);
        log.info(LogMessages.MPIN_SET_SUCCESS, mobile);

        return ValidationMessages.MPIN_SET_SUCCESS;
    }

    @Transactional
    public JwtResponse login(LoginRequest req) {
        String mobile = req.getMobile().trim();
        log.info(LogMessages.LOGIN_REQUEST, mobile);

        // Validate device info
        userUtils.validateDeviceInfo(req.getIp(), req.getDeviceId(), req.getLatitude(), req.getLongitude(), mobile);

        // Validate mobile & MPIN
        userUtils.validateMobileNotBlank(mobile);
        userUtils.validateMpinNotBlank(req.getMpin(), mobile);

        // ---------- Validate IP, Device ID, Location formats ----------
        validationUtil.validateIpFormat(req.getIp(), mobile);
        validationUtil.validateDeviceIdFormat(req.getDeviceId(), mobile);
        validationUtil.validateLocation(req.getLatitude(), String.valueOf(req.getLongitude()), mobile);

        // ---------- Fetch user ----------
        AppUser user = userUtils.getUserByMobile(mobile);

        // ---------- Verify MPIN ----------
        if (user.getMpinHash() == null || !passwordEncoder.matches(req.getMpin(), user.getMpinHash())) {
            log.warn(LogMessages.MPIN_INVALID, mobile);
            throw new IllegalArgumentException(ValidationMessages.MPIN_INVALID);
        }

        // ---------- Generate JWT access token ----------
        Map<String, Object> claims = new HashMap<>();
        claims.put("mobile", mobile);
        claims.put("deviceId", req.getDeviceId());

        String accessToken = jwtTokenService.generateAccessToken(claims, mobile);

        // ---------- Create Refresh Token ----------
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(null); // null for new token
        refreshRequest.setIp(req.getIp());
        refreshRequest.setDeviceId(req.getDeviceId());
        refreshRequest.setLatitude(Double.valueOf(String.valueOf(req.getLatitude())));
        refreshRequest.setLongitude(Double.valueOf(String.valueOf(req.getLongitude())));

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId(), refreshRequest);

        log.info(LogMessages.LOGIN_SUCCESS, mobile);
        return new JwtResponse(accessToken, refreshToken.getToken());
    }

    @Transactional
    public void resetMpin(ForgetMPINRequest req) {
        String mobile = req.getMobile() != null ? req.getMobile().trim() : null;

        log.info(LogMessages.FORGET_MPIN_REQUEST, mobile);

        // ---------- Validate mandatory fields ----------
        userUtils.validateDeviceInfo(req.getIp(), req.getDeviceId(), req.getLatitude(), req.getLongitude(), mobile);
        validationUtil.validateIpFormat(req.getIp(), mobile);
        validationUtil.validateDeviceIdFormat(req.getDeviceId(), mobile);
        if (req.getLatitude() != null && req.getLongitude() != null) {
            validationUtil.validateLocation(req.getLatitude(), String.valueOf(req.getLongitude()), mobile);
        }
        userUtils.validateMobileNotBlank(mobile);
        userUtils.validateMpinNotBlank(req.getNewMpin(), mobile);
        userUtils.validateConfirmMpinNotBlank(req.getConfirmMpin(), mobile);

        // ---------- Check MPIN match ----------
        if (!req.getNewMpin().equals(req.getConfirmMpin())) {
            log.warn(LogMessages.MPIN_MISMATCH, mobile);
            throw new IllegalArgumentException(ValidationMessages.MPIN_MISMATCH);
        }

        AppUser user = userRepo.findByMobile(mobile)
                .orElseThrow(() -> {
                    log.warn(LogMessages.USER_NOT_FOUND, mobile);
                    return new IllegalArgumentException(ValidationMessages.USER_NOT_FOUND);
                });

        // -------- Hardcoded OTP Check ----------
        if (!"123456".equals(req.getOtp())) {
//            log.warn(LogMessages.OTP_INVALID, mobile);
            throw new IllegalArgumentException(ValidationMessages.OTP_INVALID);
        }

        // -------- Update MPIN ----------
        user.setMpinHash(passwordEncoder.encode(req.getNewMpin()));
        userRepo.save(user);

        // -------- Audit ----------
        UserAudit audit = new UserAudit();
        audit.setMobile(mobile);
        audit.setIp(req.getIp());
        audit.setDeviceId(req.getDeviceId());
        audit.setLatitude(req.getLatitude());
        audit.setLongitude(req.getLongitude());
        auditRepo.save(audit);

        log.info(LogMessages.FORGET_MPIN_SUCCESS, mobile);
    }
}


