package com.example.mpin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionResponse {
    private LocalDateTime timestamp;
    private String type;
    private String description;
    private Double amount;
}
