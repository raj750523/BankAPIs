package com.example.mpin.util;

import com.example.mpin.constants.LogMessages;
import com.example.mpin.constants.ValidationMessages;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ValidationUtil {

    private static final Logger log = LoggerFactory.getLogger(ValidationUtil.class);

    private ValidationUtil() {} // prevent instantiation

    // Validate MPIN matches confirm MPIN
    public static void validateMpinMatch(String mpin, String confirmMpin, String mobile) {
        if (!mpin.equals(confirmMpin)) {
            log.warn(LogMessages.MPIN_DOES_NOT_MATCH, mobile);
            throw new IllegalArgumentException(ValidationMessages.MPIN_NOT_MATCH);
        }
    }

    // Validate user exists
    public static void validateUserExists(String mobile, Object user) {
        if (user == null) {
            log.warn(LogMessages.USER_NOT_FOUND, mobile);
            throw new IllegalArgumentException(ValidationMessages.USER_NOT_FOUND);
        }
    }
}