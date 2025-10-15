package com.example.mpin.service;

import com.example.mpin.GlobalException.GlobalException;
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
import org.springframework.http.HttpStatus;
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
    public ApiResponses<Map<String, Object>> signupStart(SignupStartRequest req) {
        String mobile = req.getMobile().trim();
        log.info(LogMessages.SIGNUP_REQUEST_RECEIVED, mobile);

        userUtils.validateDeviceInfo(req.getIp(), req.getDeviceId(), req.getLatitude(), req.getLongitude(), mobile);
        userUtils.validateMobileNotBlank(mobile);

        if (!mobile.matches("^[6-9][0-9]{9}$")) {
            log.warn(LogMessages.MOBILE_INVALID_PATTERN, mobile);
            throw new GlobalException(ValidationMessages.MOBILE_INVALID_PATTERN, HttpStatus.BAD_REQUEST.value());
        }

        validationUtil.validateIpFormat(req.getIp(), mobile);
        validationUtil.validateDeviceIdFormat(req.getDeviceId(), mobile);
        validationUtil.validateLocation(req.getLatitude(), String.valueOf(req.getLongitude()), mobile);

        if (userRepo.findByMobile(mobile).isPresent()) {
            log.warn(LogMessages.MOBILE_ALREADY_REGISTERED, mobile);
            throw new GlobalException(ValidationMessages.MOBILE_ALREADY_REGISTERED, HttpStatus.CONFLICT.value());
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
            throw new GlobalException(ValidationMessages.OTP_FAILED, HttpStatus.INTERNAL_SERVER_ERROR.value());
        }

        log.info(LogMessages.OTP_SUCCESS, mobile);

        Map<String, Object> data = Map.of(
                "mobile", mobile,
                "transactionId", "TXN" + System.currentTimeMillis(),
                "expiresIn", 300
        );

        return new ApiResponses<>("SUCCESS", HttpStatus.CREATED.value(), ValidationMessages.OTP_SENT_SUCCESS, data);
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
    public ApiResponses<Map<String, Object>> verifyOtp(VerifyOtpRequest req) {
        String mobile = req.getMobile();
        String otp = req.getOtp();

        // Fetch user and validate
        AppUser user = userUtils.getUserByMobile(mobile);
        userUtils.validateDeviceInfo(req.getIp(), req.getDeviceId(), req.getLatitude(), req.getLongitude(), mobile);
        userUtils.validateOtpNotBlank(otp, mobile);

        validationUtil.validateIpFormat(req.getIp(), mobile);
        validationUtil.validateDeviceIdFormat(req.getDeviceId(), mobile);
        validationUtil.validateLocation(req.getLatitude(), String.valueOf(req.getLongitude()), mobile);

        // Call bank API to verify OTP
        boolean verified = bankApiClient.verifyOtpWithBank(mobile, otp);
        if (!verified) {
            log.warn(LogMessages.OTP_INVALID, mobile);
            throw new GlobalException(ValidationMessages.OTP_INVALID, HttpStatus.BAD_REQUEST.value());
        }

        // Update user as OTP verified
        user.setOtpVerified(true);
        userRepo.save(user);
        log.info(LogMessages.USER_OTP_VERIFIED, mobile);
        log.info(LogMessages.OTP_VERIFIED_SUCCESS, mobile);

        // Prepare response
        Map<String, Object> data = new HashMap<>();
        data.put("mobile", mobile);
        data.put("otpVerified", true);

        return new ApiResponses<>("SUCCESS", HttpStatus.OK.value(), ValidationMessages.OTP_VERIFIED_SUCCESS, data);
    }
    @Transactional
    public ApiResponses<Map<String, Object>> setMpin(SetMpinRequest req) {
        String mobile = req.getMobile().trim();
        log.info(LogMessages.SET_MPIN_REQUEST_RECEIVED, mobile);

        // Validate device info
        userUtils.validateDeviceInfo(req.getIp(), req.getDeviceId(), req.getLatitude(), req.getLongitude(), mobile);

        // Get user
        AppUser user = userUtils.getUserByMobile(mobile);

        // Validate MPIN and Confirm MPIN
        userUtils.validateMpinNotBlank(req.getMpin(), mobile);
        userUtils.validateConfirmMpinNotBlank(req.getConfirmMpin(), mobile);

        // Validate IP, Device ID, and Location
        validationUtil.validateIpFormat(req.getIp(), mobile);
        validationUtil.validateDeviceIdFormat(req.getDeviceId(), mobile);
        validationUtil.validateLocation(req.getLatitude(), String.valueOf(req.getLongitude()), mobile);

        // Check if MPIN and Confirm MPIN match
        if (!req.getMpin().equals(req.getConfirmMpin())) {
            log.warn(LogMessages.MPIN_NOT_MATCH, mobile);
            throw new GlobalException(ValidationMessages.MPIN_NOT_MATCH, HttpStatus.BAD_REQUEST.value());
        }

        // Save MPIN
        user.setMpinHash(passwordEncoder.encode(req.getMpin()));
        userRepo.save(user);
        log.info(LogMessages.MPIN_SET_SUCCESS, mobile);

        // Prepare response data
        Map<String, Object> data = new HashMap<>();
        data.put("mobile", mobile);
        data.put("message", ValidationMessages.MPIN_SET_SUCCESS);

        return new ApiResponses<>("SUCCESS", HttpStatus.OK.value(), ValidationMessages.MPIN_SET_SUCCESS, data);
    }

    @Transactional
    public ApiResponses<Map<String, Object>> login(LoginRequest req) {
        String mobile = req.getMobile().trim();
        log.info(LogMessages.LOGIN_REQUEST, mobile);

        userUtils.validateDeviceInfo(req.getIp(), req.getDeviceId(), req.getLatitude(), req.getLongitude(), mobile);
        userUtils.validateMobileNotBlank(mobile);
        userUtils.validateMpinNotBlank(req.getMpin(), mobile);

        validationUtil.validateIpFormat(req.getIp(), mobile);
        validationUtil.validateDeviceIdFormat(req.getDeviceId(), mobile);
        validationUtil.validateLocation(req.getLatitude(), String.valueOf(req.getLongitude()), mobile);

        AppUser user = userUtils.getUserByMobile(mobile);

        if (user.getMpinHash() == null) {
            if (!"1234".equals(req.getMpin())) {
                throw new GlobalException(ValidationMessages.MPIN_INVALID, HttpStatus.UNAUTHORIZED.value());
            }
        } else if (!passwordEncoder.matches(req.getMpin(), user.getMpinHash())) {
            throw new GlobalException(ValidationMessages.MPIN_INVALID, HttpStatus.UNAUTHORIZED.value());
        }

        String accessToken = jwtTokenService.generateAccessToken(
                Map.of("mobile", mobile, "deviceId", req.getDeviceId()), mobile
        );
        RefreshTokenRequest refreshRequest = new RefreshTokenRequest();
        refreshRequest.setRefreshToken(null);
        refreshRequest.setIp(req.getIp());
        refreshRequest.setDeviceId(req.getDeviceId());
        refreshRequest.setLatitude(req.getLatitude());
        refreshRequest.setLongitude(req.getLongitude());

        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId(), refreshRequest);

        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("refreshToken", refreshToken.getToken());

        log.info(LogMessages.LOGIN_SUCCESS, mobile);
        return new ApiResponses<>("SUCCESS", HttpStatus.OK.value(), ValidationMessages.LOGIN_SUCCESS, data);
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


