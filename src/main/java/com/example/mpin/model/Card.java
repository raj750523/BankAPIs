package com.example.mpin.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

//@Entity
//@Data
//@NoArgsConstructor
//@AllArgsConstructor
//@Builder
//public class Card {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(nullable = false)
//    private String holderName;
//
//    @Column(nullable = false, unique = true, length = 16)
//    private String cardNumber;
//
//    @Column(nullable = false)
//    private String validThru; // MM/YY format
//
//    @Column(nullable = false)
//    private String mobile;
//
//    @Enumerated(EnumType.STRING)
//    private CardType type; // CREDIT or DEBIT
//}

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "card")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // card PAN — in production this should be tokenized/encrypted; demo uses plain
    @Column(nullable = false)
    private String cardNumber;

    @Column(nullable = false)
    private String holderName;

    @Column(nullable = false)
    private String validThru; // MM/yy

    // OTP related (demo)
    private String otp;

    private LocalDateTime otpExpiry;
    private int otpAttempts; // invalid attempts count

    private boolean verified;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private AppUser user;
}