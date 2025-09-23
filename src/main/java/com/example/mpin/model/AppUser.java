package com.example.mpin.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "app_user", indexes = {@Index(name = "idx_mobile", columnList = "mobile", unique = true)})
@Getter @Setter
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "mobile", length = 20, nullable = false, unique = true)
    private String mobile;

    @Column(name = "referral_code", length = 20)
    private String referralCode;

    private boolean otpVerified = false;

    @Column(name = "otp_hash")
    private String otpHash;

    private LocalDateTime sessionExpiry;
    // OTP Expiry timestamp
    private LocalDateTime otpExpiry;

    @Column(name = "session_id", length = 36, unique = true)
    private String sessionId;

    @Column(name = "mpin_hash")
    private String mpinHash;

    private String fullName;
    private String email;

    @Column(unique = true)
    private String accountNumber;

    private String ifsc;

    @Column(precision = 19, scale = 2)
    private BigDecimal balance;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Card> cards = new ArrayList<>();
}
