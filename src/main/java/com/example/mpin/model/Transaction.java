package com.example.mpin.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String transactionId;

    private String fromAccount;

    private String toAccount;

    private BigDecimal amount;

    private String remarks;

    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "from_account_id")
    private Account fromAccountEntity;

    @ManyToOne
    @JoinColumn(name = "to_account_id")
    private Account toAccountEntity;
}