package com.example.mpin.service;

import com.example.mpin.config.ApiConstants;
import com.example.mpin.dto.*;
import com.example.mpin.exception.BadRequestException;
import com.example.mpin.exception.ResourceNotFoundException;
import com.example.mpin.model.Account;
import com.example.mpin.model.AppUser;
import com.example.mpin.repository.AccountRepository;
import com.example.mpin.repository.AppUserRepository;
import com.example.mpin.repository.UserAuditRepository;
import com.example.mpin.security.JwtTokenService;
import com.example.mpin.util.JwtUtil;
import com.example.mpin.util.UserServiceUtils;
import com.example.mpin.util.ValidationUtil;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AccountService {

    private final AppUserRepository userRepo;
    private final JwtTokenService jwtService;
    private final AccountRepository repo;
    private final WebClient webClient;


    // Bank API config (ideally read from properties)
    private final String bankApiUrl = """
            https://api.bank.com/accounts/balance/""";
    private final String bankApiToken = "YOUR_BANK_API_TOKEN";

    public AccountService(AppUserRepository userRepo,
                          UserAuditRepository auditRepo,
                          JwtUtil jwtUtil,
                          ValidationUtil validationUtil,
                          UserServiceUtils userUtils, JwtTokenService jwtService, AccountRepository repo, WebClient webClient) {
        this.userRepo = userRepo;
        this.jwtService = jwtService;
        this.repo = repo;
        this.webClient = webClient;
    }

    @Transactional
    public AccountResponse addAccount(String token, AccountRequest req) {
        AppUser user = userRepo.findByMobile(jwtService.extractMobileFromHeader(token))
                .orElseThrow(() -> new ResourceNotFoundException(ApiConstants.USER_NOT_FOUND));

        repo.findByAccountNumberAndUser(req.getAccountNumber(), user)
                .ifPresent(a -> { throw new BadRequestException(ApiConstants.ACCOUNT_ALREADY_LINKED); });

        Account account = Account.builder()
                .accountNumber(req.getAccountNumber())
                .accountType(req.getAccountType().toUpperCase())
                .branchCode(req.getBranchCode())
                .balance(req.getOpeningBalance())
                .user(user)
                .active(true)
                .build();

        repo.save(account);
        log.info("Account {} linked to user {}", account.getAccountNumber(), user.getMobile());

        return new AccountResponse(
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getBranchCode(),
                account.isActive()
        );
    }

    public List<AccountResponse> getAccounts(String token) {
        AppUser user = userRepo.findByMobile(jwtService.extractMobileFromHeader(token))
                .orElseThrow(() -> new ResourceNotFoundException(ApiConstants.USER_NOT_FOUND));

        return repo.findByUser(user)
                .stream()
                .map(a -> new AccountResponse(a.getAccountNumber(), a.getAccountType(),
                        a.getBalance(), a.getBranchCode(), a.isActive()))
                .collect(Collectors.toList());
    }

    public AccountResponse getAccountByNumber(String token, String accNo) {
        AppUser user = userRepo.findByMobile(jwtService.extractMobileFromHeader(token))
                .orElseThrow(() -> new ResourceNotFoundException(ApiConstants.USER_NOT_FOUND));

        Account account = repo.findByAccountNumberAndUser(accNo, user)
                .orElseThrow(() -> new ResourceNotFoundException(ApiConstants.ACCOUNT_NOT_FOUND));

        return new AccountResponse(account.getAccountNumber(), account.getAccountType(),
                account.getBalance(), account.getBranchCode(), account.isActive());
    }

    @Transactional
    public AccountResponse getAccountBalance(String token, String accountNumber) {
        AppUser user = userRepo.findByMobile(jwtService.extractMobileFromHeader(token))
                .orElseThrow(() -> new ResourceNotFoundException(ApiConstants.USER_NOT_FOUND));

        Account account = repo.findByAccountNumberAndUser(accountNumber, user)
                .orElseThrow(() -> new ResourceNotFoundException(ApiConstants.ACCOUNT_NOT_FOUND));

        BigDecimal balance = fetchBankBalanceFromBankApi(accountNumber);
        account.setBalance(balance.doubleValue());
        repo.save(account);

        log.info("Fetched balance for account {} of user {}", accountNumber, user.getMobile());

        return new AccountResponse(account.getAccountNumber(), account.getAccountType(),
                balance.doubleValue(), account.getBranchCode(), account.isActive());
    }

    private BigDecimal fetchBankBalanceFromBankApi(String accountNumber) {
        double randomBalance = 5000 + Math.random() * 95000; // mock for demo
        return BigDecimal.valueOf(randomBalance).setScale(2, BigDecimal.ROUND_HALF_UP);
    }
}