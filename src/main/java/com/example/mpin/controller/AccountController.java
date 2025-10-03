package com.example.mpin.controller;

import com.example.mpin.dto.AccountDetailsResponse;
import com.example.mpin.dto.AccountStatementResponse;
import com.example.mpin.dto.AccountsListResponse;
import com.example.mpin.security.JwtTokenService;
import com.example.mpin.service.AccountService;
import com.example.mpin.util.UserServiceUtils;
import com.example.mpin.util.ValidationUtil;
import jakarta.annotation.Nullable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;

@RestController
@RequestMapping("/semb/api")
@Slf4j
public class AccountController {

    private ValidationUtil validationUtil;

    private final AccountService accountService;

    private final UserServiceUtils userUtils;

    private JwtTokenService jwtTokenService;


    public AccountController(AccountService accountService, UserServiceUtils userUtils
            , ValidationUtil validationUtil) {
        this.accountService = accountService;
        this.userUtils = userUtils;
        this.validationUtil = validationUtil;
    }

    @GetMapping("/accounts/list")
    public ResponseEntity<AccountsListResponse> getAccounts(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestHeader("X-IP") String ip,
            @RequestHeader(value = "X-Latitude", required = false) Double latitude,
            @RequestHeader(value = "X-Longitude", required = false) Double longitude
    ) {
        AccountsListResponse response = accountService.getAccountsList(authHeader, deviceId, ip, latitude, longitude);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/details/{id}")
    public ResponseEntity<AccountDetailsResponse> getAccountById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestHeader("X-IP") String ip,
            @RequestHeader(value = "X-Latitude", required = false) Double latitude,
            @RequestHeader(value = "X-Longitude", required = false) Double longitude) throws AccessDeniedException {

        AccountDetailsResponse response = accountService.getAccountDetailsById(id, authHeader, deviceId, ip, latitude, longitude);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/{id}/statement")
    public ResponseEntity<AccountStatementResponse> getAccountStatement(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestHeader("X-IP") String ip,
            @RequestHeader(value = "X-Latitude", required = false) Double latitude,
            @RequestHeader(value = "X-Longitude", required = false) Double longitude) throws AccessDeniedException {

        AccountStatementResponse response = accountService.getAccountStatement(
                id, authHeader, deviceId, ip, latitude, longitude
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}/statement/download")
    public ResponseEntity<byte[]> downloadStatement(
            @PathVariable Long id,
            @RequestParam String type,
            @RequestParam String ip,
            @RequestParam String deviceId,
            @RequestParam @Nullable Double latitude,
            @RequestParam @Nullable String longitude,
            @RequestHeader("Authorization") String jwt
    ) throws AccessDeniedException {
        log.info("Statement download request: userId={}, type={}, ip={}, deviceId={}", id, type, ip, deviceId);

        // ---------- JWT Validation ----------
        userUtils.validateJwt(jwt, id);

        // ---------- Device Info Validation ----------
        userUtils.validateDeviceInfo(ip, deviceId, latitude, longitude != null ? Double.valueOf(longitude) : null, String.valueOf(id));

        // ---------- IP and Device Validation ----------
        validationUtil.validateIpFormat(ip, String.valueOf(id));
        validationUtil.validateDeviceIdFormat(deviceId, String.valueOf(id));
        if (latitude != null && longitude != null) {
            validationUtil.validateLocation(latitude, longitude, String.valueOf(id));
        }

        // ---------- Generate and Stream File ----------
        return accountService.generateStatementDownload(id, type);
    }
}