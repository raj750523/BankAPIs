package com.example.mpin.controller;

import com.example.mpin.dto.FundTransferRequest;
import com.example.mpin.dto.FundTransferResponse;
import com.example.mpin.security.JwtTokenService;
import com.example.mpin.service.FundTransferService;
import com.example.mpin.util.UserServiceUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("semb/api/transfer")
@RequiredArgsConstructor
public class FundTransferController {

    private final FundTransferService service;
    private final JwtTokenService jwtService;

    @PostMapping("/imps")
    public ResponseEntity<FundTransferResponse> impsTransfer(
            @RequestHeader("Authorization") String auth,
            @RequestHeader("X-IP") String ip,
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestHeader(value = "X-Latitude", required = false) Double latitude,
            @RequestHeader(value = "X-Longitude", required = false) Double longitude,
            @Valid @RequestBody FundTransferRequest req
    ) {
        String mobile = jwtService.extractMobileFromHeader(auth);
        FundTransferResponse resp = service.transfer(req, mobile, ip, deviceId, latitude, longitude, "IMPS");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/neft")
    public ResponseEntity<FundTransferResponse> neftTransfer(
            @RequestHeader("Authorization") String auth,
            @RequestHeader("X-IP") String ip,
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestHeader(value = "X-Latitude", required = false) Double latitude,
            @RequestHeader(value = "X-Longitude", required = false) Double longitude,
            @Valid @RequestBody FundTransferRequest req
    ) {
        String mobile = jwtService.extractMobileFromHeader(auth);
        FundTransferResponse resp = service.transfer(req, mobile, ip, deviceId, latitude, longitude, "NEFT");
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/rtgs")
    public ResponseEntity<FundTransferResponse> rtgsTransfer(
            @RequestHeader("Authorization") String auth,
            @RequestHeader("X-IP") String ip,
            @RequestHeader("X-Device-Id") String deviceId,
            @RequestHeader(value = "X-Latitude", required = false) Double latitude,
            @RequestHeader(value = "X-Longitude", required = false) Double longitude,
            @Valid @RequestBody FundTransferRequest req
    ) {
        String mobile = jwtService.extractMobileFromHeader(auth);
        FundTransferResponse resp = service.transfer(req, mobile, ip, deviceId, latitude, longitude, "RTGS");
        return ResponseEntity.ok(resp);
    }
}