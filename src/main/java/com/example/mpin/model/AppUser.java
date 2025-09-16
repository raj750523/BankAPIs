package com.example.mpin.model;

import jakarta.persistence.*;
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

    @Column(nullable = false, unique = true, length = 20)
    private String mobile;

    private boolean otpVerified = false;

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
