package com.example.mpin.service;

import com.example.mpin.dto.AccountDetailsResponse;
import com.example.mpin.model.AppUser;
import com.example.mpin.model.LoginAudit;
import com.example.mpin.repository.AppUserRepository;
import com.example.mpin.repository.LoginAuditRepository;
import com.example.mpin.security.JwtTokenService;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CustomerService {

    private final AppUserRepository userRepo;
    private final LoginAuditRepository auditRepo;
    private final JwtTokenService jwtTokenService;

    public CustomerService(AppUserRepository userRepo,
                           LoginAuditRepository auditRepo,
                           JwtTokenService jwtTokenService) {
        this.userRepo = userRepo;
        this.auditRepo = auditRepo;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public AccountDetailsResponse getAccountDetailsByCustomerId(
            Long id, String authHeader, String deviceId, String ip,
            Double latitude, Double longitude) {


        String token = authHeader.replace("Bearer ", "");
        Claims claims = jwtTokenService.parseToken(token);
        String mobileFromJwt = (String) claims.get("mobile");


        AppUser user = userRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));


        if (!user.getMobile().equals(mobileFromJwt)) {
            throw new AccessDeniedException("You cannot access another customer's data");
        }


        LoginAudit audit = new LoginAudit();
        audit.setMobile(user.getMobile());
        audit.setDeviceId(deviceId);
        audit.setIp(ip);
        audit.setLatitude(latitude != null ? Double.valueOf(latitude.toString()) : null);
        audit.setLongitude(longitude != null ? Double.valueOf(longitude.toString()) : null);
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
}
