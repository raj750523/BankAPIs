package com.example.mpin.service;

import com.example.mpin.constants.LogMessages;
import com.example.mpin.constants.ValidationMessages;
import com.example.mpin.dto.FundTransferRequest;
import com.example.mpin.dto.FundTransferResponse;
import com.example.mpin.model.AppUser;
import com.example.mpin.repository.AppUserRepository;
import com.example.mpin.util.UserServiceUtils;
import com.example.mpin.util.ValidationUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class FundTransferService {

    private final AppUserRepository userRepo;
    private final UserServiceUtils userUtils;
    private final ValidationUtil validationUtil;

    private static final String DEMO_OTP = "123456";

    @Transactional
    public FundTransferResponse transfer(FundTransferRequest req, String mobile, String ip,
                                         String deviceId, Double latitude, Double longitude, String type) {

        // Validate user, OTP, device info
        AppUser user = userUtils.getUserByMobile(mobile);
        userUtils.validateDeviceInfo(ip, deviceId, latitude, longitude, mobile);
        validationUtil.validateIpFormat(ip, mobile);
        validationUtil.validateDeviceIdFormat(deviceId, mobile);
        if (latitude != null && longitude != null)
            validationUtil.validateLocation(latitude, String.valueOf(longitude), mobile);

        // Validate transfer request
        validateTransferRequest(req);
        validateOtp(req);

        // Simulate bank transfer
        String txnId = simulateBankTransfer(req, type);

        // Service **does not set HTTP code or messages** — controller and GlobalExceptionHandler handle that
        return FundTransferResponse.builder()
                .transactionId(txnId)
                .status("SUCCESS")      // service-level status
                .message(type.toUpperCase() + " transfer completed successfully")
                .build();
    }

    private void validateTransferRequest(FundTransferRequest req) {
        if (req.getFromAccount() == null || req.getFromAccount().isBlank())
            throw new IllegalArgumentException(ValidationMessages.ACCOUNT_BLANK);
        if (req.getToAccount() == null || req.getToAccount().isBlank())
            throw new IllegalArgumentException(ValidationMessages.ACCOUNT_BLANK);
        if (req.getIfsc() == null || req.getIfsc().isBlank())
            throw new IllegalArgumentException(ValidationMessages.IFSC_BLANK);
        if (req.getPayeeName() == null || req.getPayeeName().isBlank())
            throw new IllegalArgumentException(ValidationMessages.PAYEE_NAME_BLANK);
        if (req.getAmount() == null || req.getAmount() <= 0)
            throw new IllegalArgumentException(ValidationMessages.AMOUNT_INVALID);
    }

    private void validateOtp(FundTransferRequest req) {
        if (req.getOtp() == null || !req.getOtp().matches("\\d{6}") || !DEMO_OTP.equals(req.getOtp())) {
            log.warn(LogMessages.TRANSFER_FAILED, req.getFromAccount(), req.getToAccount(), req.getAmount(), "Invalid OTP");
            throw new IllegalArgumentException(ValidationMessages.OTP_INVALID);
        }
    }

    private String simulateBankTransfer(FundTransferRequest req, String type) {
        // Here, you can replace this with real bank API call.
        // For now, just simulate
        log.info(LogMessages.TRANSFER_REQUEST, req.getFromAccount(), req.getToAccount(), req.getAmount());
        return type.toUpperCase() + "-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }
}