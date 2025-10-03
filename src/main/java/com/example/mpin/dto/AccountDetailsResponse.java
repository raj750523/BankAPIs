package com.example.mpin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AccountDetailsResponse {
    private Long id;
    private String mobile;
    private String fullName;
    private String email;
    private String accountNumber;
    private String ifsc;
    private BigDecimal balance;
}


