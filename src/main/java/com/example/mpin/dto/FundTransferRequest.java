package com.example.mpin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import lombok.Data;

    @Data
    public class FundTransferRequest {
        @NotBlank
        private String fromAccount;

        @NotBlank
        private String toAccount;

        @NotBlank
        private String ifsc;

        @NotBlank
        private String payeeName;

        @Positive
        private Double amount;

        @NotBlank
        private String otp;
    }


