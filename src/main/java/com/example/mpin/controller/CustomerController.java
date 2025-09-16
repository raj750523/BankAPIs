package com.example.mpin.controller;

import com.example.mpin.dto.AccountDetailsResponse;
import com.example.mpin.model.AppUser;
import com.example.mpin.repository.AppUserRepository;
import com.example.mpin.security.JwtTokenService;
import com.example.mpin.service.CustomerService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/account")
//@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

//    @GetMapping("/{id}")
//    public ResponseEntity<AccountDetailsResponse> getAccountById(
//            @PathVariable Long id,
//            @RequestHeader("Authorization") String authHeader) {
//
//
//        String token = authHeader.replace("Bearer ", "");
//        Claims claims = jwtTokenService.parseToken(token);
//
//        String mobileFromJwt = (String) claims.get("mobile");
//
//
//        AppUser user = userRepo.findById(id)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//
//        if (!user.getMobile().equals(mobileFromJwt)) {
//            throw new AccessDeniedException("You cannot access another customer's data");
//        }
//
//        AccountDetailsResponse response = new AccountDetailsResponse(
//                user.getId(),
//                user.getMobile(),
//                user.getFullName(),
//                user.getEmail(),
//                user.getAccountNumber(),
//                user.getIfsc(),
//                user.getBalance()
//        );
//
//        return ResponseEntity.ok(response);
//    }
//}

    @GetMapping("/{id}")
    public ResponseEntity<AccountDetailsResponse> getAccountById(
            @PathVariable Long id,
            @RequestHeader("Authorization") String authHeader,
            @RequestHeader(value = "X-Device-Id") String deviceId,
            @RequestHeader(value = "X-IP-Address") String ip,
            @RequestHeader(value = "X-Latitude", required = false) Double latitude,
            @RequestHeader(value = "X-Longitude", required = false) Double longitude) {

        return ResponseEntity.ok(
                customerService.getAccountDetailsByCustomerId(
                        id, authHeader, deviceId, ip, latitude, longitude
                )
        );
    }
}
