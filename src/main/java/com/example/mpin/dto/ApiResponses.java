package com.example.mpin.dto;

import java.time.LocalDateTime;

public class ApiResponses<T> {
    private  final String status;
    private int code;
    private String message;
    private T data;
    private LocalDateTime timestamp;



    public  ApiResponses(String status, int code, String message, T data) {
        this.status = status;
        this.code = code;
        this.message = message;
        this.data = data;
        this.timestamp = LocalDateTime.now();
    }

    // Getters & Setters
    public String getStatus() { return status; }
    public int getCode() { return code; }
    public String getMessage() { return message; }
    public T getData() { return data; }
    public LocalDateTime getTimestamp() { return timestamp; }
}

