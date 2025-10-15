package com.example.mpin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AccountResponse {
    private String accountNumber;
    private String accountType;
    private Double balance;
    private String branchCode;
    private boolean active;
}