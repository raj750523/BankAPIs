package com.example.mpin.bankapi;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BankApiClient {

    public boolean sendOtp(String mobile) {
        log.debug("Sending OTP request to bank for mobile: {}", mobile);
        try {
            // TODO: Replace with actual HTTP request to bank’s SMS/OTP API
            log.debug("Bank API response: success=true for mobile {}", mobile);
            return true;
        } catch (Exception e) {
            log.error("Bank API call failed for mobile: {}", mobile, e);
            return false;
        }
    }

    // Mock verify OTP request
    public boolean verifyOtpWithBank(String mobile, String otp) {
        log.debug("Sending OTP verification request to bank for mobile: {}, otp: {}", mobile, otp);

        try {

            if ("1234".equals(otp)) { // Dummy check for local testing
                log.debug("Bank API OTP verification success for mobile {}", mobile);
                return true;
            } else {
                log.warn("Bank API OTP verification failed for mobile: {}, invalid otp: {}", mobile, otp);
                return false;
            }
        } catch (Exception e) {
            log.error("Bank API OTP verification request failed for mobile: {}", mobile, e);
            return false;
        }
    }
}