package com.example.mpin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ForgetMPINRequest {

    @NotBlank
    private String mobile;

    @NotBlank
    private String otp;

    @NotBlank
    private String newMpin;
}
