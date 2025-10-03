package com.example.mpin.dto;

import lombok.Data;

@Data
public class ResendOtpRequest {


    private String mobile;

    private String ip;

    private String deviceId;

    private Double latitude;

    private Double longitude;
}