package com.example.mpin.repository;

import com.example.mpin.model.Account;
import com.example.mpin.model.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUser(AppUser user);
    Optional<Account> findByAccountNumberAndUser(String accountNumber, AppUser user);
}