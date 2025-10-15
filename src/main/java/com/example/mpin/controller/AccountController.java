package com.example.mpin.controller;

import com.example.mpin.config.ApiConstants;
import com.example.mpin.dto.*;
import com.example.mpin.exception.BadRequestException;
import com.example.mpin.security.JwtTokenService;
import com.example.mpin.service.AccountService;
import com.example.mpin.util.UserServiceUtils;
import com.example.mpin.util.ValidationUtil;
import jakarta.annotation.Nullable;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.AccessDeniedException;
import java.util.List;

@RestController
@RequestMapping("/semb/api/accounts")
@Slf4j
public class AccountController {

    private final AccountService accountService;
    private JwtTokenService jwtTokenService;


    public AccountController(AccountService accountService, UserServiceUtils userUtils
            , ValidationUtil validationUtil) {
        this.accountService = accountService;
    }

    @PostMapping("/add")
    public ResponseEntity<ApiResponse<AccountResponse>> addAccount(
            @RequestHeader(value = "Authorization", required = true) String auth,
            @RequestBody @Valid AccountRequest req) {


        AccountResponse resp = accountService.addAccount(auth, req);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(resp));
    }

    @GetMapping("/listOfAccounts")
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getAllAccounts(
            @RequestHeader("Authorization") String auth) {

        List<AccountResponse> resp = accountService.getAccounts(auth);
        return ResponseEntity.ok(new ApiResponse<>(resp));
    }

    @GetMapping("/by-number/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountByNumber(
            @RequestHeader("Authorization") String auth,
            @PathVariable String accountNumber) {

        AccountResponse resp = accountService.getAccountByNumber(auth, accountNumber);
        return ResponseEntity.ok(new ApiResponse<>(resp));
    }

    @GetMapping("/balance/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccountBalance(
            @RequestHeader("Authorization") String auth,
            @PathVariable String accountNumber) {

        AccountResponse resp = accountService.getAccountBalance(auth, accountNumber);
        return ResponseEntity.ok(new ApiResponse<>(resp));
    }
}