package com.example.mpin.constants;

public class ValidationMessages {
    private ValidationMessages() {
    }

    public static final String MOBILE_BLANK = "Mobile number cannot be blank";
    public static final String MOBILE_INVALID_PATTERN = "Mobile number must be exactly 10 digits";
    public static final String MOBILE_ALREADY_REGISTERED = "Mobile number already registered";
    public static final String OTP_FAILED = "Failed to send OTP via Bank API for mobile";
    public static final String OTP_SENT_SUCCESS = "OTP sent successfully";

    //Verify Otp
    public static final String USER_NOT_FOUND = "User not found";
    public static final String OTP_BLANK = "OTP cannot be blank";
    public static final String OTP_INVALID = "Invalid OTP";
    public static final String OTP_VERIFIED_SUCCESS = "OTP verified successfully";

    //Resend OTP
    public static final String MOBILE_ALREADY_VERIFIED = "Mobile already verified, please login";
    public static final String OTP_RESENT_SUCCESS = "OTP resent successfully";

    // MPIN related
    public static final String MPIN_BLANK = "MPIN cannot be blank";
    public static final String CONFIRM_MPIN_BLANK = "Confirm MPIN cannot be blank";
    public static final String MPIN_NOT_MATCH = "MPIN and Confirm MPIN do not match";
    public static final String MPIN_SET_SUCCESS = "MPIN set successfully";
    public static final String MPIN_INVALID_PATTERN = "MPIN must be 4-6 digits numeric";
    public static final String CONFIRM_MPIN_INVALID_PATTERN = "Confirm MPIN must be 4-6 digits numeric";


    // Login
    public static final String MPIN_INVALID = "Invalid MPIN";
    public static final String LOGIN_SUCCESS = "Login successful";

    // Device info validation messages
    public static final String IP_BLANK = "IP cannot be blank";
    public static final String DEVICE_ID_BLANK = "Device ID cannot be blank";
    public static final String LATITUDE_BLANK = "Latitude cannot be blank";
    public static final String LONGITUDE_BLANK = "Longitude cannot be blank";
    public static final String LOCATION_INVALID = "Invalid Latitude or Longitude value";


    // ---------- IP ----------
    public static final String IP_INVALID = "Invalid IP format";

    // ---------- Device ID ----------
    public static final String DEVICE_ID_INVALID = "Invalid device ID format";


    public static final String JWT_INVALID = "Invalid or expired JWT token.";

    public static final String ACCESS_DENIED = "You cannot access another customer's data.";

    public static final String INVALID_REFRESH_TOKEN = "Invalid refresh token";
    public static final String REFRESH_TOKEN_EXPIRED = "Refresh token expired";
    public static final String LOGOUT_SUCCESS = "User logged out successfully";

    // Forget MPIN
    public static final String MPIN_RESET_SUCCESS = "MPIN reset successfully. Please login with new MPIN.";
    public static final String MPIN_MISMATCH = "MPIN not match to confirmMpin";

    public static final String INVALID_DATE_RANGE = "Invalid date range";

}

