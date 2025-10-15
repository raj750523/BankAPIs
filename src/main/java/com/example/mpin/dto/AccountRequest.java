package com.example.mpin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AccountRequest {
    @NotBlank(message = "Account number is required")
    private String accountNumber;

    @NotBlank(message = "Account type is required")
    private String accountType;

    private String branchCode;

    private Double openingBalance = 0.0;
}