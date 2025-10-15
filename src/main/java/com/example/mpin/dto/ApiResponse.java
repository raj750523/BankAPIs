package com.example.mpin.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {
    private String status; // SUCCESS or FAILED
    private String errorCode;
    private String message;
    private T data;

    // Constructor for **successful response** (no hardcoded status/message)
    public ApiResponse(T data) {
        this.status = "SUCCESS";
        this.data = data;
        this.errorCode = null;
        this.message = null;
    }

    // Constructor for **failure** handled by GlobalExceptionHandler
    public ApiResponse(String status, String errorCode, String message) {
        this.status = status;
        this.errorCode = errorCode;
        this.message = message;
        this.data = null;
    }
}