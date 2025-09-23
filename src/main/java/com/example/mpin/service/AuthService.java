package com.example.mpin.service;

import com.example.mpin.bankapi.BankApiClient;
import com.example.mpin.constants.LogMessages;
import com.example.mpin.constants.ValidationMessages;
import com.example.mpin.dto.*;
import com.example.mpin.model.AppUser;
import com.example.mpin.model.LoginAudit;
import com.example.mpin.model.RefreshToken;
import com.example.mpin.repository.AppUserRepository;
import com.example.mpin.repository.LoginAuditRepository;
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
    private final LoginAuditRepository auditRepo;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final BankApiClient bankApiClient;
    private final RestTemplate restTemplate;
    private final UserServiceUtils userUtils;

    private final RefreshTokenService refreshTokenService;


    @Value("${bank.api.base-url:http://localhost:9090/mock-bank}")
    private String bankApiBase;

    public AuthService(AppUserRepository userRepo,
                       LoginAuditRepository auditRepo,
                       JwtTokenService jwtTokenService,
                       PasswordEncoder passwordEncoder,
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
    }

    @Transactional
    public String signupStart(SignupStartRequest req) {
        String mobile = req.getMobile().trim();
        log.info(LogMessages.SIGNUP_REQUEST_RECEIVED, mobile);

        // Validate mobile
        userUtils.validateMobileNotBlank(mobile);

        String localPattern = "^[6-9][0-9]{9}$";
        if (!mobile.matches(localPattern)) {
            log.warn(LogMessages.MOBILE_INVALID_PATTERN, mobile);
            throw new IllegalArgumentException(ValidationMessages.MOBILE_INVALID_PATTERN);
        }

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

        AppUser user = userUtils.getUserByMobile(mobile);
        if (user.isOtpVerified()) {
            log.warn(LogMessages.MOBILE_ALREADY_VERIFIED, mobile);
            throw new IllegalArgumentException(ValidationMessages.MOBILE_ALREADY_VERIFIED);
        }

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
        userUtils.validateOtpNotBlank(otp, mobile);

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

        AppUser user = userUtils.getUserByMobile(mobile);

        // Validate MPIN
        userUtils.validateMpinNotBlank(req.getMpin(), mobile);

        userUtils.validateConfirmMpinNotBlank(req.getConfirmMpin(), mobile);

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

        userUtils.validateMobileNotBlank(mobile);
        userUtils.validateMpinNotBlank(req.getMpin(), mobile);

        AppUser user = userUtils.getUserByMobile(mobile);

        if (user.getMpinHash() == null || !passwordEncoder.matches(req.getMpin(), user.getMpinHash())) {
            log.warn(LogMessages.MPIN_INVALID, mobile);
            throw new IllegalArgumentException(ValidationMessages.MPIN_INVALID);
        }

        LoginAudit audit = new LoginAudit();
        audit.setMobile(mobile);
        audit.setIp(req.getIp());
        audit.setDeviceId(req.getDeviceId());
        audit.setLocation(req.getLocation());
        audit.setLatitude(req.getLatitude());
        audit.setLongitude(req.getLongitude());
        auditRepo.save(audit);

        Map<String, Object> claims = new HashMap<>();
        claims.put("mobile", mobile);
        claims.put("ip", req.getIp());
        claims.put("deviceId", req.getDeviceId());
        claims.put("location", req.getLocation());

        String accessToken = jwtTokenService.generateAccessToken(claims, mobile);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());

        log.info(LogMessages.LOGIN_SUCCESS, mobile);

        return new JwtResponse(accessToken, refreshToken.getToken());
    }

    public AccountDetailsResponse getProfileByMobile(
            String mobile,
            String ip,
            String deviceId,
            Double latitude,
            Double longitude
    ) {
        AppUser user = userRepo.findByMobile(mobile)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Store audit info
        LoginAudit audit = new LoginAudit();
        audit.setMobile(mobile);
        audit.setIp(ip);
        audit.setDeviceId(deviceId);
        audit.setLatitude(latitude);
        audit.setLongitude(longitude);
        auditRepo.save(audit);

        return new AccountDetailsResponse(
                user.getId(),
                user.getMobile(),
                user.getFullName(),
                user.getEmail(),
                user.getAccountNumber(),
                user.getIfsc(),
                user.getBalance()
        );
    }

    @Transactional
    public void resetMpin(ForgetMPINRequest req) {
        AppUser user = userRepo.findByMobile(req.getMobile())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!"123456".equals(req.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }
        user.setMpinHash(passwordEncoder.encode(req.getNewMpin()));
        userRepo.save(user);
    }
}

