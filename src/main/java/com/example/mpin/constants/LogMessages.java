package com.example.mpin.constants;

public class LogMessages {


    private LogMessages() {
    } // prevent instantiation

    public static final String SIGNUP_REQUEST_RECEIVED = "{\"event\":\"signup_request\",\"mobile\":\"{}\"}";
    public static final String MOBILE_BLANK = "{\"event\":\"mobile_blank\",\"mobile\":\"{}\"}";
    public static final String MOBILE_INVALID_PATTERN = "{\"event\":\"invalid_mobile_pattern\",\"mobile\":\"{}\"}";
    public static final String MOBILE_ALREADY_REGISTERED = "{\"event\":\"mobile_already_registered\",\"mobile\":\"{}\"}";
    public static final String USER_SAVED =  "{\"event\":\"user_saved\",\"mobile\":\"{}\"}";
    public static final String CALLING_BANK_API = "{\"event\":\"calling_bank_api\",\"mobile\":\"{}\"}";
    public static final String OTP_FAILED = "{\"event\":\"otp_failed\",\"mobile\":\"{}\"}";
    public static final String OTP_SUCCESS = "{\"event\":\"otp_sent_success\",\"mobile\":\"{}\"}";


    //Verify OTP
    public static final String USER_NOT_FOUND = "{\"event\":\"user_not_found\",\"mobile\":\"{}\"}";
    public static final String OTP_BLANK = "{\"event\":\"otp_blank\",\"mobile\":\"{}\"}";
    public static final String OTP_INVALID = "{\"event\":\"otp_invalid\",\"mobile\":\"{}\"}";
    public static final String USER_OTP_VERIFIED = "{\"event\":\"otp_verified_success\",\"mobile\":\"{}\"}";
    public static final String OTP_VERIFIED_SUCCESS = "{\"event\":\"otp_verified_success\",\"mobile\":\"{}\"}";

    //Resend OTP
    public static final String MOBILE_ALREADY_VERIFIED = "{\"event\":\"mobile_already_verified\",\"mobile\":\"{}\"}";
    public static final String OTP_RESENT =  "{\"event\":\"otp_resent\",\"mobile\":\"{}\"}";
    public static final String OTP_RESENT_SUCCESS =  "{\"event\":\"otp_resent_success\",\"mobile\":\"{}\"}";

    //referral Code
    public static final String REFERRAL_CODE_SAVED = "{\"event\":\"referral_code_saved\",\"referralCode\":\"{}\",\"mobile\":\"{}\"}";

    // MPIN related
    public static final String SET_MPIN_REQUEST_RECEIVED = "{\"event\":\"set_mpin_request\",\"mobile\":\"{}\"}";
    public static final String MPIN_DOES_NOT_MATCH = "{\"event\":\"mpin_not_match\",\"mobile\":\"{}\"}";;
    public static final String CONFIRM_MPIN_BLANK = "{\"event\":\"confirm_mpin_blank\",\"mobile\":\"{}\"}";
    public static final String MPIN_SET_SUCCESS = "{\"event\":\"mpin_set_success\",\"mobile\":\"{}\"}";
    public static final String MPIN_BLANK = "{\"event\":\"mpin_blank\",\"mobile\":\"{}\"}";
    public static final String MPIN_INVALID = "{\"event\":\"mpin_invalid\",\"mobile\":\"{}\"}";
    public static final String MPIN_NOT_MATCH = "{\"event\":\"mpin_not_match\",\"mobile\":\"{}\"}";

//login
    public static final String LOGIN_REQUEST = "{\"event\":\"login_request\",\"mobile\":\"{}\"}";
    public static final String LOGIN_SUCCESS = "{\"event\":\"login_success\",\"mobile\":\"{}\"}";
}

