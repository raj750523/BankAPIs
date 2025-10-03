package com.example.mpin.controller;

import com.example.mpin.constants.LogMessages;
import com.example.mpin.constants.ValidationMessages;
import com.example.mpin.dto.*;
import com.example.mpin.model.RefreshToken;
import com.example.mpin.security.JwtTokenService;
import com.example.mpin.service.AuthService;
import com.example.mpin.service.CustomerService;
import com.example.mpin.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/semb/api")
@Slf4j
public class AuthController {

    private final AuthService authService;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;
    private final CustomerService customerService;

    public AuthController(AuthService authService, JwtTokenService jwtTokenService, RefreshTokenService refreshTokenService,CustomerService customerService) {
        this.authService = authService;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
        this.customerService = customerService;
    }

    @PostMapping("/signup")
    public ResponseEntity<String> signupStart(@RequestBody @Valid SignupStartRequest req) {
        return ResponseEntity.ok(authService.signupStart(req));
    }

    @PostMapping("/signupVerify")
    public ResponseEntity<String> verifyOtp(@RequestBody @Valid VerifyOtpRequest req) {
        return ResponseEntity.ok(authService.verifyOtp(req));
    }

    @PostMapping("/signupResendOtp")
    public ResponseEntity<String> resendOtp(@RequestBody @Valid ResendOtpRequest req) {
        return ResponseEntity.ok(authService.resendOtp(req));
    }

    @PostMapping("/signupSetMpin")
    public ResponseEntity<String> setMpin(@RequestBody @Valid SetMpinRequest req) {
        return ResponseEntity.ok(authService.setMpin(req));
    }

    @PostMapping("/signupLogin")
    public ResponseEntity<JwtResponse> login(@RequestBody @Valid LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @GetMapping("/signupProfile")
    public ResponseEntity<AccountDetailsResponse> me(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestHeader("X-IP") String ip,
            @RequestHeader(value = "X-Latitude", required = false) Double latitude,
            @RequestHeader(value = "X-Longitude", required = false) Double longitude) {

        AccountDetailsResponse profile = customerService.getProfileByJwt(
                authHeader, ip, deviceId, latitude, longitude
        );
        return ResponseEntity.ok(profile);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDetailsResponse> getAccountById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestHeader("X-IP") String ip,
            @RequestHeader(value = "X-Latitude", required = false) Double latitude,
                @RequestHeader(value = "X-Longitude", required = false) Double longitude) {

        AccountDetailsResponse response = customerService.getAccountDetailsByCustomerId(
                id, authHeader, deviceId, ip, latitude, longitude
        );
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh-token")
    public JwtResponse refreshToken(@RequestBody RefreshTokenRequest request) {
        return refreshTokenService.refreshToken(request);
    }

    @PostMapping("/logout")
    public String logout(@RequestBody LogoutRequest request) {
       return refreshTokenService.logout(request);
    }

    @PostMapping("/forget-mpin")
    public ResponseEntity<String> forgetMpin(@RequestBody @Valid ForgetMPINRequest request) {
        authService.resetMpin(request);
        return ResponseEntity.ok(ValidationMessages.MPIN_RESET_SUCCESS);
    }
}
