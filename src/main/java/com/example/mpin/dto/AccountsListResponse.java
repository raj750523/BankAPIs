package com.example.mpin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccountsListResponse {
    private String mobile;
    private List<AccountInfo> accounts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AccountInfo {
        private Long accountId;
        private String accountType; // Savings, Current, FD, etc.
        private String accountNumber;
        private String ifsc;
        private BigDecimal balance;
    }

}