package com.example.mpin.util;

import com.example.mpin.constants.LogMessages;
import com.example.mpin.constants.ValidationMessages;
import com.example.mpin.model.AppUser;
import com.example.mpin.repository.AppUserRepository;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.nio.file.AccessDeniedException;

@Slf4j
@Service
public class UserServiceUtils {

    private final AppUserRepository userRepo;

    private final JwtUtil jwtUtil;


    public UserServiceUtils(AppUserRepository userRepo, JwtUtil jwtUtil) {
        this.userRepo = userRepo;
        this.jwtUtil = jwtUtil;
    }

    public AppUser getUserByMobile(String mobile) {
        return userRepo.findByMobile(mobile).orElseThrow(() -> {
            log.warn(LogMessages.USER_NOT_FOUND, mobile);
            return new RuntimeException(ValidationMessages.USER_NOT_FOUND);
        });
    }

    // ---------------- Get user by ID ----------------
    public AppUser getUserById(Long id) {
        return userRepo.findById(id).orElseThrow(() -> {
//            log.warn(LogMessages.USER_NOT_FOUND, "id=" + id);
            return new RuntimeException(ValidationMessages.USER_NOT_FOUND);
        });
    }

    // Validate mobile is not blank
    public void validateMobileNotBlank(String mobile) {
        if (mobile == null || mobile.isBlank()) {
            log.warn(LogMessages.MOBILE_BLANK, mobile);
            throw new IllegalArgumentException(ValidationMessages.MOBILE_BLANK);
        }
    }

    // Validate OTP is not blank
    public void validateOtpNotBlank(String otp, String mobile) {
        if (otp == null || otp.isBlank()) {
            log.warn(LogMessages.OTP_BLANK, mobile);
            throw new IllegalArgumentException(ValidationMessages.OTP_BLANK);
        }
    }

    // Validate MPIN is not blank
    public void validateMpinNotBlank(String mpin, String mobile) {
        if (mpin == null || mpin.isBlank()) {
            log.warn(LogMessages.MPIN_BLANK, mobile);
            throw new IllegalArgumentException(ValidationMessages.MPIN_BLANK);
        }
        if (!mpin.matches("\\d{4,6}")) {
            log.warn(LogMessages.MPIN_INVALID_PATTERN, mobile);
            throw new IllegalArgumentException(ValidationMessages.MPIN_INVALID_PATTERN);
        }

    }

    // Validate Confirm MPIN is not blank
    public void validateConfirmMpinNotBlank(String confirmMpin, String mobile) {
        if (confirmMpin == null || confirmMpin.isBlank()) {
            log.warn(LogMessages.CONFIRM_MPIN_BLANK, mobile);
            throw new IllegalArgumentException(ValidationMessages.CONFIRM_MPIN_BLANK);
        }
        if (!confirmMpin.matches("\\d{4,6}")) {
            log.warn(LogMessages.CONFIRM_MPIN_INVALID_PATTERN, mobile);
            throw new IllegalArgumentException(ValidationMessages.CONFIRM_MPIN_INVALID_PATTERN);
        }
    }

    public void validateDeviceInfo(String ip, String deviceId, Double latitude, Double longitude, String mobile) {
        // IP check
        if (ip == null || ip.isBlank()) {
            log.warn(LogMessages.IP_BLANK, mobile);
            throw new IllegalArgumentException(ValidationMessages.IP_BLANK);
        }

        // ---------- Device ID ----------
        if (deviceId == null || deviceId.isBlank()) {
            log.warn(LogMessages.DEVICE_ID_BLANK, mobile);
            throw new IllegalArgumentException(ValidationMessages.DEVICE_ID_BLANK);
        }

        // ---------- Latitude ----------
        if (latitude == null) {
            log.warn(LogMessages.LATITUDE_BLANK, mobile);
            throw new IllegalArgumentException(ValidationMessages.LATITUDE_BLANK);
        }

        // ---------- Longitude ----------
        if (longitude == null) {
            log.warn(LogMessages.LONGITUDE_BLANK, mobile);
            throw new IllegalArgumentException(ValidationMessages.LONGITUDE_BLANK);
        }
        // Optional: check valid ranges
        if (latitude < -90 || latitude > 90 || longitude < -180 || longitude > 180) {
            throw new IllegalArgumentException("Invalid Latitude or Longitude value");
        }
    }
    public void validateJwt(String authHeader, Long userId) throws AccessDeniedException {
        Claims claims = jwtUtil.getClaimsFromHeader(authHeader);
        String mobileFromJwt = claims.get("mobile", String.class);

        AppUser user = getUserById(userId);

        if (!user.getMobile().equals(mobileFromJwt)) {
            log.error(LogMessages.ACCESS_DENIED, mobileFromJwt);
            throw new AccessDeniedException(ValidationMessages.ACCESS_DENIED);
        }

//        log.info(LogMessages.JWT_VALIDATED, mobileFromJwt);
    }

}