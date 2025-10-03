package com.example.mpin.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter @Setter
public class UserAudit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;

    private String mobile;

    private String ip;

    private String deviceId;

    private String location;

    private Double latitude;

    private Double longitude;

    @CreationTimestamp
    private LocalDateTime loginTime;
}
