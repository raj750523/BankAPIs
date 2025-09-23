package com.example.mpin.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class SignupStartRequest {

//    @NotBlank(message = "Mobile number is required")
//    @Size(min = 10, max = 10, message = "Mobile number must be 10 digits")
//    @Pattern(
//            regexp = "^[6-9][0-9]{9}$"
    /// /            message = "Invalid mobile number. Must be 10 digits starting with 6–9"
//    )
    private String mobile;

    private String referralCode;

    @NotBlank
    private String ip;

    @NotBlank
    private String deviceId;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;
}
