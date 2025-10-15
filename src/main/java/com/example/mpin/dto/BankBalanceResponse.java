package com.example.mpin.dto;

import java.math.BigDecimal;

public class BankBalanceResponse {
    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BankBalanceResponse(BigDecimal balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "BankBalanceResponse{" +
                "balance=" + balance +
                '}';
    }

    private BigDecimal balance;
}
