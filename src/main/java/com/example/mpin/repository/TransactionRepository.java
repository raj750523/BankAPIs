package com.example.mpin.repository;

import com.example.mpin.model.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    @Query(value = "SELECT t FROM Transaction t WHERE t.fromAccount = :accountNumber OR t.toAccount = :accountNumber ORDER BY t.createdAt DESC")
    List<Transaction> findTopNByFromAccountOrToAccountOrderByCreatedAtDesc(String accountNumber);
}