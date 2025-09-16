package com.example.mpin.dto;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}