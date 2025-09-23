package com.example.mpin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SetMpinRequest {
    @NotBlank
    private String mobile;


    private String mpin;

    private String confirmMpin;

    @NotBlank(message = "IP address is required")
    private String ip;

    @NotBlank(message = "Device ID is required")
    private String deviceId;

    private Double latitude;
    private Double longitude;
}
