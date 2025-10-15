package com.example.mpin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class AccountDetailsResponse {
    private Long id;
    private String mobile;
    private String fullName;
    private String email;
    private String accountNumber;
    private String ifsc;
    private String bankName;
    private BigDecimal balance;

    private List<AccountResponse> accounts;
}


