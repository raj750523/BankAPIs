package com.example.mpin.service;

import com.example.mpin.constants.LogMessages;
import com.example.mpin.constants.ValidationMessages;
import com.example.mpin.dto.AccountDetailsResponse;
import com.example.mpin.model.AppUser;
import com.example.mpin.model.UserAudit;
import com.example.mpin.repository.AppUserRepository;
import com.example.mpin.repository.UserAuditRepository;
import com.example.mpin.security.JwtTokenService;
import com.example.mpin.util.JwtUtil;
import com.example.mpin.util.UserServiceUtils;
import com.example.mpin.util.ValidationUtil;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class CustomerService {

    private final AppUserRepository userRepo;
    private final UserAuditRepository auditRepo;
    private final JwtUtil jwtUtil;
    private final ValidationUtil validationUtil;
    private final UserServiceUtils userUtils;

    public CustomerService(AppUserRepository userRepo,
                           UserAuditRepository auditRepo,
                           JwtUtil jwtUtil,
                           ValidationUtil validationUtil, UserServiceUtils userUtils) {
        this.userRepo = userRepo;
        this.auditRepo = auditRepo;
        this.jwtUtil = jwtUtil;
        this.validationUtil = validationUtil;
        this.userUtils = userUtils;
    }

    @Transactional
    public AccountDetailsResponse getProfileByJwt(
            String authHeader,
            String ip,
            String deviceId,
            Double latitude,
            Double longitude) {

        String mobileFromJwt = jwtUtil.getMobileFromHeader(authHeader);

        // Validate
        validateDeviceInfo(ip, deviceId, latitude, longitude, mobileFromJwt);

        // Fetch user by mobile
        AppUser user = userUtils.getUserByMobile(mobileFromJwt);

        return buildAccountDetailsResponse(user, deviceId, ip, latitude, longitude);
    }

    // -------- ACCOUNT BY ID --------
    @Transactional
    public AccountDetailsResponse getAccountDetailsByCustomerId(
            Long id,
            String authHeader,
            String deviceId,
            String ip,
            Double latitude,
            Double longitude) {

        String mobileFromJwt = jwtUtil.getMobileFromHeader(authHeader);

        // Validate
        validateDeviceInfo(ip, deviceId, latitude, longitude, mobileFromJwt);

        // Fetch user by ID
        AppUser user = userRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn(LogMessages.USER_NOT_FOUND, "id=" + id);
                    return new RuntimeException(ValidationMessages.USER_NOT_FOUND);
                });

        // Ensure JWT user matches target user
        if (!user.getMobile().equals(mobileFromJwt)) {
            log.error(LogMessages.ACCESS_DENIED, mobileFromJwt);
            throw new AccessDeniedException(ValidationMessages.ACCESS_DENIED);
        }

        return buildAccountDetailsResponse(user, deviceId, ip, latitude, longitude);
    }

    // -------- PRIVATE HELPERS --------
    private void validateDeviceInfo(String ip, String deviceId, Double latitude, Double longitude, String mobile) {
        userUtils.validateDeviceInfo(ip, deviceId, latitude, longitude, mobile);
        validationUtil.validateIpFormat(ip, mobile);
        validationUtil.validateDeviceIdFormat(deviceId, mobile);
        validationUtil.validateLocation(latitude, String.valueOf(longitude), mobile);
    }

    private AccountDetailsResponse buildAccountDetailsResponse(AppUser user,
                                                               String deviceId,
                                                               String ip,
                                                               Double latitude,
                                                               Double longitude) {
        // Save audit
        UserAudit audit = new UserAudit();
        audit.setMobile(user.getMobile());
        audit.setDeviceId(deviceId);
        audit.setIp(ip);
        audit.setLatitude(latitude);
        audit.setLongitude(longitude);
        auditRepo.save(audit);

        log.info(LogMessages.GET_ACCOUNT_SUCCESS, user.getMobile());

        // Build response
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
}