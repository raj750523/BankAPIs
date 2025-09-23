package com.example.mpin.constants;

public class ValidationMessages {
    private ValidationMessages() {}

    public static final String MOBILE_BLANK = "Mobile number cannot be blank";
    public static final String MOBILE_INVALID_PATTERN = "Mobile number must be exactly 10 digits";
//    public static final String COUNTRY_CODE = "Invalid country code or mobile number";
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


    // Login
    public static final String MPIN_INVALID = "Invalid MPIN";
    public static final String LOGIN_SUCCESS = "Login successful";
}