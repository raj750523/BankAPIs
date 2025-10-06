package com.example.mpin.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
    public class FundTransferResponse {
    private String status;
    private String message;
    private String transactionId;
    }
