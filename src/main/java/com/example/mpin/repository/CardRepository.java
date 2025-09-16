package com.example.mpin.repository;

import com.example.mpin.model.AppUser;
import com.example.mpin.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

//public interface CardRepository extends JpaRepository<Card, Long> {
//}


public interface CardRepository extends JpaRepository<Card, Long> {
    List<Card> findByUser(AppUser user);
    Optional<Card> findByIdAndUser(Long id, AppUser user);
    void deleteByUser(AppUser user);
}