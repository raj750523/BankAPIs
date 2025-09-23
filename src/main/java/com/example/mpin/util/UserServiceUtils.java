package com.example.mpin.util;

import com.example.mpin.constants.LogMessages;
import com.example.mpin.constants.ValidationMessages;
import com.example.mpin.model.AppUser;
import com.example.mpin.repository.AppUserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UserServiceUtils {

    private final AppUserRepository userRepo;

    public UserServiceUtils(AppUserRepository userRepo) {
        this.userRepo = userRepo;
    }

    public AppUser getUserByMobile(String mobile) {
        return userRepo.findByMobile(mobile).orElseThrow(() -> {
            log.warn(LogMessages.USER_NOT_FOUND, mobile);
            return new RuntimeException(ValidationMessages.USER_NOT_FOUND);
        });
    }

    // Validate mobile is not blank
    public void validateMobileNotBlank(String mobile) {
        if (mobile == null || mobile.isBlank()) {
            log.warn(LogMessages.MOBILE_BLANK, mobile);
            throw new IllegalArgumentException(ValidationMessages.MOBILE_BLANK);
        }
    }

    // Validate OTP is not blank
    public void validateOtpNotBlank(String otp, String mobile) {
        if (otp == null || otp.isBlank()) {
            log.warn(LogMessages.OTP_BLANK, mobile);
            throw new IllegalArgumentException(ValidationMessages.OTP_BLANK);
        }
    }

    // Validate MPIN is not blank
    public void validateMpinNotBlank(String mpin, String mobile) {
        if (mpin == null || mpin.isBlank()) {
            log.warn(LogMessages.MPIN_BLANK, mobile);
            throw new IllegalArgumentException(ValidationMessages.MPIN_BLANK);
        }
    }

    // Validate Confirm MPIN is not blank
    public void validateConfirmMpinNotBlank(String confirmMpin, String mobile) {
        if (confirmMpin == null || confirmMpin.isBlank()) {
            log.warn(LogMessages.CONFIRM_MPIN_BLANK, mobile);
            throw new IllegalArgumentException(ValidationMessages.CONFIRM_MPIN_BLANK);
        }
    }
}