package com.example.mpin.service;

import com.example.mpin.constants.LogMessages;
import com.example.mpin.constants.ValidationMessages;
import com.example.mpin.dto.AccountDetailsResponse;
import com.example.mpin.dto.AccountResponse;
import com.example.mpin.dto.ApiResponses;
import com.example.mpin.model.Account;
import com.example.mpin.model.AppUser;
import com.example.mpin.model.UserAudit;
import com.example.mpin.repository.AccountRepository;
import com.example.mpin.repository.AppUserRepository;
import com.example.mpin.repository.UserAuditRepository;
import com.example.mpin.util.JwtUtil;
import com.example.mpin.util.UserServiceUtils;
import com.example.mpin.util.ValidationUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class CustomerService {

    private final AppUserRepository userRepo;
    private final AccountRepository accountRepo;
    private final UserAuditRepository auditRepo;
    private final JwtUtil jwtUtil;
    private final ValidationUtil validationUtil;
    private final UserServiceUtils userUtils;

    public CustomerService(AppUserRepository userRepo,
                           UserAuditRepository auditRepo,
                           JwtUtil jwtUtil,
                           ValidationUtil validationUtil, UserServiceUtils userUtils,AccountRepository accountRepo) {
        this.userRepo = userRepo;
        this.auditRepo = auditRepo;
        this.jwtUtil = jwtUtil;
        this.validationUtil = validationUtil;
        this.userUtils = userUtils;
        this.accountRepo = accountRepo;
    }

    @Transactional
    public ApiResponses<Map<String, Object>> getProfile(String authHeader,
                                                        String ip,
                                                        String deviceId,
                                                        Double latitude,
                                                        Double longitude) {

        String mobileFromJwt = jwtUtil.getMobileFromHeader(authHeader);

        // Validate device/IP/location
        validateDeviceInfo(ip, deviceId, latitude, longitude, mobileFromJwt);

        AppUser user = userUtils.getUserByMobile(mobileFromJwt);

        List<Account> accounts = accountRepo.findByUser(user);
        if (accounts.isEmpty()) {
            accounts = fetchBankAccountsFromBank(mobileFromJwt);
            accounts.forEach(acc -> {
                acc.setUser(user);
                accountRepo.save(acc);
            });
        }

        AccountDetailsResponse details = buildAccountDetailsResponse(user, accounts, deviceId, ip, latitude, longitude);

        Map<String, Object> data = new HashMap<>();
        data.put("user", details);

        return new ApiResponses<>("SUCCESS", HttpStatus.OK.value(), "Profile fetched successfully", data);
    }

    @Transactional
    public ApiResponses<Map<String, Object>> getAccountById(Long id,
                                                            String authHeader,
                                                            String deviceId,
                                                            String ip,
                                                            Double latitude,
                                                            Double longitude) {

        String mobileFromJwt = jwtUtil.getMobileFromHeader(authHeader);

        validateDeviceInfo(ip, deviceId, latitude, longitude, mobileFromJwt);

        AppUser user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException(ValidationMessages.USER_NOT_FOUND));

        if (!user.getMobile().equals(mobileFromJwt)) {
            throw new AccessDeniedException(ValidationMessages.ACCESS_DENIED);
        }

        List<Account> accounts = accountRepo.findByUser(user);

        AccountDetailsResponse details = buildAccountDetailsResponse(user, accounts, deviceId, ip, latitude, longitude);

        Map<String, Object> data = new HashMap<>();
        data.put("user", details);

        return new ApiResponses<>("SUCCESS", HttpStatus.OK.value(), "Account details fetched successfully", data);
    }

    private void validateDeviceInfo(String ip, String deviceId, Double latitude, Double longitude, String mobile) {
        userUtils.validateDeviceInfo(ip, deviceId, latitude, longitude, mobile);
        validationUtil.validateIpFormat(ip, mobile);
        validationUtil.validateDeviceIdFormat(deviceId, mobile);
        validationUtil.validateLocation(latitude, String.valueOf(longitude), mobile);
    }

    private List<Account> fetchBankAccountsFromBank(String mobile) {
        return List.of(
                Account.builder()
                        .accountNumber("123456789012")
                        .accountType("SAVINGS")
                        .balance(15000.0)
                        .branchCode("BR001")
                        .active(true)
                        .build(),
                Account.builder()
                        .accountNumber("987654321098")
                        .accountType("CURRENT")
                        .balance(250000.0)
                        .branchCode("BR002")
                        .active(true)
                        .build()
        );
    }

    private AccountDetailsResponse buildAccountDetailsResponse(AppUser user,
                                                               List<Account> accounts,
                                                               String deviceId,
                                                               String ip,
                                                               Double latitude,
                                                               Double longitude) {

        // Save audit log
        UserAudit audit = new UserAudit();
        audit.setMobile(user.getMobile());
        audit.setDeviceId(deviceId);
        audit.setIp(ip);
        audit.setLatitude(latitude);
        audit.setLongitude(longitude);
        auditRepo.save(audit);

        List<AccountResponse> accountResponses = accounts.stream()
                .map(acc -> new AccountResponse(
                        acc.getAccountNumber(),
                        acc.getAccountType(),
                        acc.getBalance(),
                        acc.getBranchCode(),
                        acc.isActive()
                ))
                .collect(Collectors.toList());

        return new AccountDetailsResponse(
                user.getId(),
                user.getMobile(),
                user.getFullName(),
                user.getEmail(),
                user.getAccountNumber(),
                user.getIfsc(),
                user.getBankName(),
                user.getBalance(),
                accountResponses
        );
    }
}