package com.example.mpin.service;

import com.example.mpin.dto.CardRequest;
import com.example.mpin.exception.ResourceNotFoundException;
import com.example.mpin.model.AppUser;
import com.example.mpin.model.Card;
import com.example.mpin.model.CardType;
import com.example.mpin.repository.AppUserRepository;
import com.example.mpin.repository.CardRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

//@Service
//public class CardService {
//
//    private final CardRepository repo;
//
//    public CardService(CardRepository repo) {
//        this.repo = repo;
//    }
//
//    @Transactional
//    public Card addCard(CardRequest req) {
//        Card card = Card.builder()
//                .holderName(req.getHolderName())
//                .cardNumber(req.getCardNumber())
//                .validThru(req.getValidThru())
//                .mobile(req.getMobile())
//                .type(CardType.valueOf(req.getType().toUpperCase()))
//                .build();
//        return repo.save(card);
//    }
//
//    public List<Card> getAllCards() {
//        return repo.findAll();
//    }
//
//    public Card getCardById(Long id) {
//        return repo.findById(id)
//                .orElseThrow(() -> new RuntimeException("Card not found"));
//    }
//}

@Service
public class CardService {

    private final CardRepository repo;
    private final AppUserRepository userRepo;

    public CardService(CardRepository repo, AppUserRepository userRepo) {
        this.repo = repo;
        this.userRepo = userRepo;
    }

    @Transactional
    public Card addCard(CardRequest req, String mobile) {
        AppUser user = userRepo.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Card card = Card.builder()
                .holderName(req.getHolderName())
                .cardNumber(req.getCardNumber())
                .validThru(req.getValidThru())
                .type(CardType.valueOf(req.getType().toUpperCase()))
                .user(user)
                .build();

        return repo.save(card);
    }

    public List<Card> getCardsForUser(String mobile) {
        AppUser user = userRepo.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        return repo.findByUser(user);
    }

    public Card getCardById(Long id, String mobile) {
        AppUser user = userRepo.findByMobile(mobile)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return repo.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Card not found or not owned by user"));
    }
}