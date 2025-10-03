package com.example.mpin.repository;

import com.example.mpin.model.UserAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAuditRepository extends JpaRepository<UserAudit, Long> {
}
