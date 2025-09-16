package com.example.mpin.controller;

import com.example.mpin.dto.*;
import com.example.mpin.model.RefreshToken;
import com.example.mpin.security.JwtTokenService;
import com.example.mpin.service.AuthService;
import com.example.mpin.service.RefreshTokenService;
import io.jsonwebtoken.Claims;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final AuthService authService;
    private final JwtTokenService jwtTokenService;
    private final RefreshTokenService refreshTokenService;

    public AuthController(AuthService authService, JwtTokenService jwtTokenService, RefreshTokenService refreshTokenService) {
        this.authService = authService;
        this.jwtTokenService = jwtTokenService;
        this.refreshTokenService = refreshTokenService;
    }

    @PostMapping("/signup/start")
    public ResponseEntity<String> signupStart(@RequestBody @Valid SignupStartRequest req) {
        return ResponseEntity.ok(authService.signupStart(req));
    }

    @PostMapping("/signup/verify")
    public ResponseEntity<String> verifyOtp(@RequestBody @Valid VerifyOtpRequest req) {
        return ResponseEntity.ok(authService.verifyOtp(req));
    }

    @PostMapping("/signup/set-mpin")
    public ResponseEntity<String> setMpin(@RequestBody @Valid SetMpinRequest req) {
        return ResponseEntity.ok(authService.setMpin(req));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<JwtResponse> login(@RequestBody @Valid LoginRequest req) {
        return ResponseEntity.ok(authService.login(req));
    }

    @GetMapping("/profile/me")
    public ResponseEntity<AccountDetailsResponse> me(
            @RequestHeader("Authorization") String bearer,
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestHeader("X-IP") String ip,
            @RequestHeader(value = "X-Latitude", required = false) Double latitude,
            @RequestHeader(value = "X-Longitude", required = false) Double longitude
    ) {
        String token = bearer.replace("Bearer ", "");
        Claims claims = jwtTokenService.parseToken(token);
        String mobile = claims.get("mobile", String.class);

        // Pass all info to service
        AccountDetailsResponse profile = authService.getProfileByMobile(mobile, ip, deviceId, latitude, longitude);
        return ResponseEntity.ok(profile);
    }

    @PostMapping("/refresh-token")
    public JwtResponse refreshToken(@RequestBody Map<String, String> request) {
        String refreshTokenStr = request.get("refreshToken");
        RefreshToken token = refreshTokenService.findByToken(refreshTokenStr)
                .map(refreshTokenService::verifyExpiration)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));
        String accessToken = jwtTokenService.generateAccessToken(
                Map.of("mobile", token.getUser().getMobile()),
                token.getUser().getMobile()
        );
        return new JwtResponse(accessToken, refreshTokenStr);
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout(@RequestBody Map<String, String> request) {
        String refreshToken = request.get("refreshToken");
        if (refreshToken == null) {
            return ResponseEntity.badRequest().body("Refresh token is required");
        }
        refreshTokenService.deleteByToken(refreshToken);
        return ResponseEntity.ok("User logged out successfully");
    }
    @PostMapping("/forget-mpin")
    public ResponseEntity<String> forgetMpin(@RequestBody @Valid ForgetMPINRequest request) {
        authService.resetMpin(request);
        return ResponseEntity.ok("MPIN reset successfully. Please login with new MPIN.");
    }

}
