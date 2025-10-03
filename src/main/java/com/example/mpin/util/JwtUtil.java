package com.example.mpin.util;

import com.example.mpin.constants.LogMessages;
import com.example.mpin.constants.ValidationMessages;
import com.example.mpin.security.JwtTokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class JwtUtil {


    private final JwtTokenService jwtTokenService;

    public JwtUtil(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    public Claims getClaimsFromHeader(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.error(LogMessages.JWT_INVALID, "Missing or invalid Bearer header");
            throw new IllegalArgumentException(ValidationMessages.JWT_INVALID);
        }

        String token = authHeader.replace("Bearer ", "");
        try {
            return jwtTokenService.parseToken(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.error(LogMessages.JWT_INVALID, e.getMessage());
            throw new IllegalArgumentException(ValidationMessages.JWT_INVALID);
        }
    }

    public String getMobileFromHeader(String authHeader) {
        Claims claims = getClaimsFromHeader(authHeader);
        return claims.get("mobile", String.class);
    }

}
