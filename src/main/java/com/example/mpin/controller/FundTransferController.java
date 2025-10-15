package com.example.mpin.controller;

import com.example.mpin.dto.ApiResponse;
import com.example.mpin.dto.FundTransferRequest;
import com.example.mpin.dto.FundTransferResponse;
import com.example.mpin.security.JwtTokenService;
import com.example.mpin.service.FundTransferService;
import com.example.mpin.util.UserServiceUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("semb/api/transfer")
@RequiredArgsConstructor
public class FundTransferController {

    private final FundTransferService service;
    private final JwtTokenService jwtService;

    @PostMapping("/{type}")
    public ResponseEntity<ApiResponse<FundTransferResponse>> transfer(
            @RequestHeader("Authorization") String auth,
            @RequestHeader("X-IP") String ip,
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestHeader(value = "X-Latitude", required = false) Double latitude,
            @RequestHeader(value = "X-Longitude", required = false) Double longitude,
            @PathVariable String type,
            @Valid @RequestBody FundTransferRequest req
    ) {
        String mobile = jwtService.extractMobileFromHeader(auth);


        FundTransferResponse resp = service.transfer(req, mobile, ip, deviceId, latitude, longitude, type);

        // Wrap automatically in ApiResponse with default success values
        return ResponseEntity.ok(new ApiResponse<>(resp));
    }
}