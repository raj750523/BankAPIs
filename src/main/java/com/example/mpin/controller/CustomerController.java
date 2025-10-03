//package com.example.mpin.controller;
//
//import com.example.mpin.dto.AccountDetailsResponse;
//import com.example.mpin.service.CustomerService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/account")
////@RequiredArgsConstructor
//public class CustomerController {
//
//    private final CustomerService customerService;
//
//    public CustomerController(CustomerService customerService) {
//        this.customerService = customerService;
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<AccountDetailsResponse> getAccountById(
//            @PathVariable Long id,
//            @RequestHeader("Authorization") String authHeader,
//            @RequestHeader(value = "X-Device-Id") String deviceId,
//            @RequestHeader(value = "X-IP") String ip,
//            @RequestHeader(value = "X-Latitude", required = false) Double latitude,
//            @RequestHeader(value = "X-Longitude", required = false) Double longitude) {
//
//        AccountDetailsResponse response = customerService.getAccountDetailsByCustomerId(
//                id, authHeader, deviceId, ip, latitude, longitude
//        );
//        return ResponseEntity.ok(response);
//    }
//}