package com.example.mpin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CardResponse {
    private Long id;
    private String holderName;
    private String maskedCardNumber;
    private String validThru;
    private boolean verified;
}