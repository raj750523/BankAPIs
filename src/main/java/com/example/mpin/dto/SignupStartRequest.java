package com.example.mpin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class SignupStartRequest {

    @NotBlank @Pattern(regexp = "^[0-9]{10}$")
    private String mobile;

    @NotBlank
    private String ip;

    @NotBlank
    private String deviceId;

    @NotBlank
    private Double latitude;

    @NotBlank
    private Double longitude;
}
