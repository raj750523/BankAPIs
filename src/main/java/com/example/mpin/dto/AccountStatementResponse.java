package com.example.mpin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountStatementResponse {
    private Long accountId;
    private String accountNumber;
    private String ifsc;
    private List<Transaction> transactions;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class Transaction {
        private LocalDateTime date;
        private String type; // CREDIT/DEBIT
        private String description;
        private BigDecimal amount;
        private BigDecimal balanceAfter;
    }
}