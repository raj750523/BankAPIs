package com.example.mpin.service;

import com.example.mpin.constants.LogMessages;
import com.example.mpin.constants.ValidationMessages;
import com.example.mpin.dto.AccountDetailsResponse;
import com.example.mpin.dto.AccountStatementResponse;
import com.example.mpin.dto.AccountsListResponse;
import com.example.mpin.model.AppUser;
import com.example.mpin.model.UserAudit;
import com.example.mpin.repository.AppUserRepository;
import com.example.mpin.repository.UserAuditRepository;
import com.example.mpin.util.JwtUtil;
import com.example.mpin.util.UserServiceUtils;
import com.example.mpin.util.ValidationUtil;
import com.itextpdf.io.source.ByteArrayOutputStream;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
public class AccountService {

    private final AppUserRepository userRepo;
    private final UserAuditRepository auditRepo;
    private final JwtUtil jwtUtil;
    private final ValidationUtil validationUtil;
    private final UserServiceUtils userUtils;

    private static final String DOWNLOAD_DIR = System.getProperty("user.home") + "/Downloads/";

    public AccountService(AppUserRepository userRepo,
                          UserAuditRepository auditRepo,
                          JwtUtil jwtUtil,
                          ValidationUtil validationUtil,
                          UserServiceUtils userUtils) {
        this.userRepo = userRepo;
        this.auditRepo = auditRepo;
        this.jwtUtil = jwtUtil;
        this.validationUtil = validationUtil;
        this.userUtils = userUtils;
    }

    @Transactional
    public AccountsListResponse getAccountsList(String authHeader,
                                                String deviceId,
                                                String ip,
                                                Double latitude,
                                                Double longitude) {
        // ---------- Validate JWT ----------
        Claims claims = jwtUtil.getClaimsFromHeader(authHeader);
        String mobile = claims.get("mobile", String.class);

        // ---------- Validate device info ----------
        userUtils.validateDeviceInfo(ip, deviceId, latitude, longitude, mobile);
        validationUtil.validateIpFormat(ip, mobile);
        validationUtil.validateDeviceIdFormat(deviceId, mobile);
        if (latitude != null && longitude != null) {
            validationUtil.validateLocation(latitude, String.valueOf(longitude), mobile);
        }

        // ---------- Fetch user ----------
        AppUser user = userUtils.getUserByMobile(mobile);

        // ---------- Audit ----------
        UserAudit audit = new UserAudit();
        audit.setMobile(mobile);
        audit.setDeviceId(deviceId);
        audit.setIp(ip);
        audit.setLatitude(latitude);
        audit.setLongitude(longitude);
        auditRepo.save(audit);

        log.info(LogMessages.GET_ACCOUNTS_SUCCESS, mobile);

        // ---------- Prepare accounts list ----------
        List<AccountsListResponse.AccountInfo> accounts = List.of(
                new AccountsListResponse.AccountInfo(user.getId(), "Savings", user.getAccountNumber(),
                        user.getIfsc(), user.getBalance())
                // Add more accounts if the user has multiple
        );

        return new AccountsListResponse(mobile, accounts);
    }
    @Transactional
    public AccountDetailsResponse getAccountDetailsById(Long id, String authHeader, String deviceId, String ip,
                                                        Double latitude, Double longitude) throws AccessDeniedException {

        String mobileFromJwt = jwtUtil.getMobileFromHeader(authHeader);

        userUtils.validateDeviceInfo(ip, deviceId, latitude, longitude, mobileFromJwt);
        validationUtil.validateIpFormat(ip, mobileFromJwt);
        validationUtil.validateDeviceIdFormat(deviceId, mobileFromJwt);
        if (latitude != null && longitude != null) {
            validationUtil.validateLocation(latitude, String.valueOf(longitude), mobileFromJwt);
        }

        AppUser user = userRepo.findById(id)
                .orElseThrow(() -> {
                    log.warn(LogMessages.USER_NOT_FOUND, "id=" + id);
                    return new IllegalArgumentException(ValidationMessages.USER_NOT_FOUND);
                });

        if (!user.getMobile().equals(mobileFromJwt)) {
            log.error(LogMessages.ACCESS_DENIED, mobileFromJwt);
            throw new AccessDeniedException(ValidationMessages.ACCESS_DENIED);
        }

        // Audit log

        log.info(LogMessages.GET_ACCOUNT_SUCCESS, user.getMobile());

        return new AccountDetailsResponse(
                user.getId(),
                user.getMobile(),
                user.getFullName(),
                user.getEmail(),
                user.getAccountNumber(),
                user.getIfsc(),
                new BigDecimal(String.valueOf(user.getBalance()))
        );
    }
    @Transactional
    public AccountStatementResponse getAccountStatement(Long accountId,
                                                        String authHeader,
                                                        String deviceId,
                                                        String ip,
                                                        Double latitude,
                                                        Double longitude) throws AccessDeniedException {

        String mobileFromJwt = jwtUtil.getMobileFromHeader(authHeader);

        // Device/IP/location validations
        userUtils.validateDeviceInfo(ip, deviceId, latitude, longitude, mobileFromJwt);
        validationUtil.validateIpFormat(ip, mobileFromJwt);
        validationUtil.validateDeviceIdFormat(deviceId, mobileFromJwt);
        if (latitude != null && longitude != null) {
            validationUtil.validateLocation(latitude, String.valueOf(longitude), mobileFromJwt);
        }

        // Fetch user/account
        AppUser user = userRepo.findById(accountId).orElseThrow(() -> {
            log.warn(LogMessages.USER_NOT_FOUND, "id=" + accountId);
            return new IllegalArgumentException(ValidationMessages.USER_NOT_FOUND);
        });

        if (!user.getMobile().equals(mobileFromJwt)) {
            log.error(LogMessages.ACCESS_DENIED, mobileFromJwt);
            throw new AccessDeniedException(ValidationMessages.ACCESS_DENIED);
        }

        // Audit
        UserAudit audit = new UserAudit();
        audit.setMobile(user.getMobile());
        audit.setDeviceId(deviceId);
        audit.setIp(ip);
        audit.setLatitude(latitude);
        audit.setLongitude(longitude);
        auditRepo.save(audit);

        // Mock transaction data for demonstration
        List<AccountStatementResponse.Transaction> transactions = List.of(
                new AccountStatementResponse.Transaction(
                        LocalDateTime.now().minusDays(2), "DEBIT", "Grocery Shopping", new BigDecimal("1500.00"), new BigDecimal("8500.00")
                ),
                new AccountStatementResponse.Transaction(
                        LocalDateTime.now().minusDays(1), "CREDIT", "Salary", new BigDecimal("5000.00"), new BigDecimal("13500.00")
                )
        );

        log.info(LogMessages.ACCOUNT_STATEMENT_FETCHED, user.getMobile());

        return new AccountStatementResponse(
                user.getId(),
                user.getAccountNumber(),
                user.getIfsc(),
                transactions
        );
    }
    public ResponseEntity<byte[]> generateStatementDownload(Long userId, String type) {
        try {
            if ("pdf".equalsIgnoreCase(type)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                PdfWriter writer = new PdfWriter(baos);
                PdfDocument pdf = new PdfDocument(writer);
                Document document = new Document(pdf);

                document.add(new Paragraph("Account Statement").setBold().setFontSize(16));
                document.add(new Paragraph("User ID: " + userId));

                Table table = new Table(new float[]{100, 300, 100, 100});
                table.addHeaderCell("Date");
                table.addHeaderCell("Description");
                table.addHeaderCell("Credit");
                table.addHeaderCell("Debit");

                table.addCell(LocalDate.now().toString());
                table.addCell("Salary Credit");
                table.addCell("5000");
                table.addCell("");

                table.addCell(LocalDate.now().minusDays(1).toString());
                table.addCell("ATM Withdrawal");
                table.addCell("");
                table.addCell("1000");

                document.add(table);
                document.close();

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_PDF);
                headers.setContentDispositionFormData("attachment", "statement_" + userId + ".pdf");

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(baos.toByteArray());

            } else if ("excel".equalsIgnoreCase(type)) {
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                try (XSSFWorkbook workbook = new XSSFWorkbook()) {
                    Sheet sheet = workbook.createSheet("Statement");

                    Row header = sheet.createRow(0);
                    header.createCell(0).setCellValue("Date");
                    header.createCell(1).setCellValue("Description");
                    header.createCell(2).setCellValue("Credit");
                    header.createCell(3).setCellValue("Debit");

                    Row row1 = sheet.createRow(1);
                    row1.createCell(0).setCellValue(LocalDate.now().toString());
                    row1.createCell(1).setCellValue("Salary Credit");
                    row1.createCell(2).setCellValue(5000);
                    row1.createCell(3).setCellValue("");

                    Row row2 = sheet.createRow(2);
                    row2.createCell(0).setCellValue(LocalDate.now().minusDays(1).toString());
                    row2.createCell(1).setCellValue("ATM Withdrawal");
                    row2.createCell(2).setCellValue("");
                    row2.createCell(3).setCellValue(1000);

                    workbook.write(baos);
                }

                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
                headers.setContentDispositionFormData("attachment", "statement_" + userId + ".xlsx");

                return ResponseEntity.ok()
                        .headers(headers)
                        .body(baos.toByteArray());

            } else {
                throw new IllegalArgumentException("Invalid type. Must be pdf or excel.");
            }
        } catch (Exception e) {
            log.error("Failed to generate statement for userId={}: {}", userId, e.getMessage());
            throw new RuntimeException("Unable to generate statement");
        }
    }
}