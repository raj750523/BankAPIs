package com.example.mpin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgetMPINRequest {

    private String mobile;

    private String otp;

    private String newMpin;

    private String confirmMpin;

    private String ip;

    private String deviceId;

    private Double latitude;

    private Double longitude;
}
