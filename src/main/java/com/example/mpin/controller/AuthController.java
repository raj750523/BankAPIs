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
import org.springframework.http.HttpStatus;
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
    public ResponseEntity<ApiResponses<?>> signupStart(@RequestBody @Valid SignupStartRequest req) {
        ApiResponses<Map<String, Object>> response = authService.signupStart(req);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/signupVerify")
    public ResponseEntity<ApiResponses<Map<String, Object>>> verifyOtp(@RequestBody @Valid VerifyOtpRequest req) {
        ApiResponses<Map<String, Object>> response = authService.verifyOtp(req);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }


    @PostMapping("/signupResendOtp")
    public ResponseEntity<String> resendOtp(@RequestBody @Valid ResendOtpRequest req) {
        return ResponseEntity.ok(authService.resendOtp(req));
    }

    @PostMapping("/signupSetMpin")
    public ResponseEntity<ApiResponses<Map<String, Object>>> setMpin(@RequestBody @Valid SetMpinRequest req) {
        ApiResponses<Map<String, Object>> response = authService.setMpin(req);
        return ResponseEntity.status(HttpStatus.OK).body(response); // explicitly 200 OK
    }

    @PostMapping("/signupLogin")
    public ResponseEntity<ApiResponses<Map<String, Object>>> login(@RequestBody @Valid LoginRequest req) {
        ApiResponses<Map<String, Object>> response = authService.login(req);
        return ResponseEntity.status(HttpStatus.OK).body(response); // 200 OK
    }
    @GetMapping("/signupProfile")
    public ResponseEntity<ApiResponses<Map<String, Object>>> getProfile(
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestHeader("X-IP") String ip,
            @RequestHeader(value = "X-Latitude", required = false) Double latitude,
            @RequestHeader(value = "X-Longitude", required = false) Double longitude
    ) {
        ApiResponses<Map<String, Object>> response = customerService.getProfile(authHeader, ip, deviceId, latitude, longitude);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @GetMapping("/signupProfile/{id}")
    public ResponseEntity<ApiResponses<Map<String, Object>>> getAccountById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestHeader("X-IP") String ip,
            @RequestHeader(value = "X-Latitude", required = false) Double latitude,
            @RequestHeader(value = "X-Longitude", required = false) Double longitude
    ) {
        ApiResponses<Map<String, Object>> response = customerService.getAccountById(id, authHeader, deviceId, ip, latitude, longitude);
        return ResponseEntity.status(HttpStatus.OK).body(response);
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
