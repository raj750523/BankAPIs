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
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"cardNumber"})
})
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String holderName;

    @Column(nullable = false, length = 16, unique = true)
    private String cardNumber;

    @Column(nullable = false)
    private String validThru; // MM/YY

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CardType type; // CREDIT or DEBIT

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @com.fasterxml.jackson.annotation.JsonIgnore
    private AppUser user;
}