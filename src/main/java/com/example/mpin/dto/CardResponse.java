package com.example.mpin.dto;

import lombok.Data;

@Data
public class CardResponse {
    private Long id;
    private String holderName;
    private String cardNumber;
    private String validThru;
    private String type;
}