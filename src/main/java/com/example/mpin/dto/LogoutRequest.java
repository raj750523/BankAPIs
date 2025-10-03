package com.example.mpin.dto;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;

    private String ip;

    private String deviceId;

    private Double latitude;

    private Double longitude;
}