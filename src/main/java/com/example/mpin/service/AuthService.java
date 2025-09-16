package com.example.mpin.service;

import com.example.mpin.dto.*;
import com.example.mpin.model.AppUser;
import com.example.mpin.model.LoginAudit;
import com.example.mpin.model.RefreshToken;
import com.example.mpin.repository.AppUserRepository;
import com.example.mpin.repository.LoginAuditRepository;
import com.example.mpin.security.JwtTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    private final AppUserRepository userRepo;
    private final LoginAuditRepository auditRepo;
    private final JwtTokenService jwtTokenService;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;
//    private final JwtTokenBlacklistService jwtTokenBlacklistService;
    private final RefreshTokenService refreshTokenService;


    @Value("${bank.api.base-url:http://localhost:9090/mock-bank}")
    private String bankApiBase;

    public AuthService(AppUserRepository userRepo,
                       LoginAuditRepository auditRepo,
                       JwtTokenService jwtTokenService,
                       PasswordEncoder passwordEncoder,
                       RestTemplate restTemplate,

                       RefreshTokenService refreshTokenService, RefreshTokenService refreshTokenService1) {
        this.userRepo = userRepo;
        this.auditRepo = auditRepo;
        this.jwtTokenService = jwtTokenService;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
        this.refreshTokenService = refreshTokenService1;
    }

    @Transactional
    public String signupStart(SignupStartRequest req) {
        userRepo.findByMobile(req.getMobile()).orElseGet(() -> {
            AppUser u = new AppUser();
            u.setMobile(req.getMobile());
            return userRepo.save(u);
        });
        LoginAudit audit = new LoginAudit();
        audit.setMobile(req.getMobile());
        audit.setIp(req.getIp());
        audit.setDeviceId(req.getDeviceId());
        audit.setLatitude(req.getLatitude());
        audit.setLongitude(req.getLongitude());
        auditRepo.save(audit);

        // In real system → call Bank API to send OTP
        return "OTP sent successfully";
    }

    @Transactional
    public String verifyOtp(VerifyOtpRequest req) {
        AppUser user = userRepo.findByMobile(req.getMobile())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate OTP (dummy check for dev)
        if (!"1234".equals(req.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        // Update user status
        user.setOtpVerified(true);
        userRepo.save(user);

        // Log OTP verification attempt
        LoginAudit audit = new LoginAudit();
        audit.setMobile(req.getMobile());
        audit.setIp(req.getIp());
        audit.setDeviceId(req.getDeviceId());
        // Optional: include latitude/longitude if available
        audit.setLatitude(req.getLatitude());
        audit.setLongitude(req.getLongitude());

        // Do NOT set location
        // audit.setLocation("OTP Verified"); // removed

        auditRepo.save(audit);

        return "OTP verified successfully";
    }

    @Transactional
    public String setMpin(SetMpinRequest req) {
        AppUser user = userRepo.findByMobile(req.getMobile())
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate OTP (dummy check)
        if (!"1234".equals(req.getOtp())) {
            throw new RuntimeException("Invalid OTP");
        }

        // Set MPIN
        user.setMpinHash(passwordEncoder.encode(req.getMpin()));

//        // Optional: store IP, device, location info
//        user.setIp(req.getIp());
//        user.setDeviceId(req.getDeviceId());
//        user.setLatitude(req.getLatitude());
//        user.setLongitude(req.getLongitude());
//
//        userRepo.save(user);

        // Optional: log this action
        LoginAudit audit = new LoginAudit();
        audit.setMobile(req.getMobile());
        audit.setIp(req.getIp());
        audit.setDeviceId(req.getDeviceId());
        audit.setLatitude(req.getLatitude());
        audit.setLongitude(req.getLongitude());
        auditRepo.save(audit);

        return "MPIN set successfully";
    }


    @Transactional
    public JwtResponse login(LoginRequest req) {
        AppUser user = userRepo.findByMobile(req.getMobile())
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getMpinHash() == null || !passwordEncoder.matches(req.getMpin(), user.getMpinHash())) {
            throw new RuntimeException("Invalid MPIN");
        }


        LoginAudit audit = new LoginAudit();
        audit.setMobile(req.getMobile());
        audit.setIp(req.getIp());
        audit.setDeviceId(req.getDeviceId());
        audit.setLocation(req.getLocation());
        audit.setLatitude(req.getLatitude());
        audit.setLongitude(req.getLongitude());
        auditRepo.save(audit);


        Map<String, Object> claims = new HashMap<>();
        claims.put("mobile", req.getMobile());
        claims.put("ip", req.getIp());
        claims.put("deviceId", req.getDeviceId());
        claims.put("location", req.getLocation());


        String accessToken = jwtTokenService.generateAccessToken(claims, req.getMobile());
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(user.getId());
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

