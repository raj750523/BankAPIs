package com.example.mpin.controller;

import com.example.mpin.dto.CardRequest;
import com.example.mpin.model.Card;
import com.example.mpin.security.JwtTokenService;
import com.example.mpin.service.CardService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

//@RestController
//@RequestMapping("/api/cards")
//public class CardController {
//
//    private final CardService service;
//
//    public CardController(CardService service) {
//        this.service = service;
//    }
//
//    @PostMapping
//    public ResponseEntity<Card> addCard(@RequestBody @Valid CardRequest req) {
//        return ResponseEntity.ok(service.addCard(req));
//    }
//
//    @GetMapping
//    public ResponseEntity<List<Card>> getAllCards() {
//        return ResponseEntity.ok(service.getAllCards());
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Card> getCardById(@PathVariable Long id) {
//        return ResponseEntity.ok(service.getCardById(id));
//    }
//}


@RestController
@RequestMapping("/api/cards")
public class CardController {

    private final CardService service;
    private final JwtTokenService jwtService;

    public CardController(CardService service, JwtTokenService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
    }

    @PostMapping
    public ResponseEntity<Card> addCard(@RequestHeader("Authorization") String auth,
                                        @Valid @RequestBody CardRequest req) {
        String mobile = jwtService.extractMobileFromHeader(auth);
        return ResponseEntity.ok(service.addCard(req, mobile));
    }

    @GetMapping
    public ResponseEntity<List<Card>> getCards(@RequestHeader("Authorization") String auth) {
        String mobile = jwtService.extractMobileFromHeader(auth);
        return ResponseEntity.ok(service.getCardsForUser(mobile));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Card> getCard(@RequestHeader("Authorization") String auth,
                                        @PathVariable Long id) {
        String mobile = jwtService.extractMobileFromHeader(auth);
        return ResponseEntity.ok(service.getCardById(id, mobile));
    }
}