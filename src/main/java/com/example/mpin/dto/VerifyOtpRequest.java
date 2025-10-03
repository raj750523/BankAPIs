package com.example.mpin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class VerifyOtpRequest {

    private String mobile;

    private String otp;

    private String ip;

    private String deviceId;

    private Double latitude;

    private Double longitude;
}
