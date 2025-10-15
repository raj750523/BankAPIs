package com.example.mpin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class FundTransferRequest {

    private String fromAccount;

    private String toAccount;


    private String ifsc;


    private String payeeName;

    @Positive
    private Double amount;

    @NotBlank
    private String otp;
}
