package com.example.mpin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank @Pattern(regexp = "^[0-9]{10}$")
    private String mobile;

    @NotBlank @Size(min = 4, max = 6)
    private String mpin;
    @NotBlank private String ip;
    @NotBlank private String deviceId;
    @NotBlank private String location;
    @NotBlank private Double latitude;
    @NotBlank private Double longitude;
}
