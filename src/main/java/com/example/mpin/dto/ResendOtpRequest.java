package com.example.mpin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class ResendOtpRequest {

//    @NotBlank(message = "Mobile number is required")
//    @Pattern(regexp = "^[0-9]{10}$", message = "Mobile must be 10 digits")
    private String mobile;

    @NotBlank(message = "IP address is required")
    private String ip;

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    private Double latitude;
    private Double longitude;
}