package com.example.mpin.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SetMpinRequest {

    private String mobile;

    private String mpin;

    private String confirmMpin;

    private String ip;

    private String deviceId;

    private Double latitude;

    private Double longitude;
}
