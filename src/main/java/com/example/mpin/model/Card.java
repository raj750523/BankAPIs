package com.example.mpin.model;

import jakarta.persistence.*;
import lombok.*;

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
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String holderName;

    private String cardID;

    @Enumerated(EnumType.STRING)
    private CardType type;

    private String validThru;

    private String otp;

    private boolean verified;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private AppUser user;
}