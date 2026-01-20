package com.wif3006.banking_system.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wif3006.banking_system.dto.card.CreateCardDto;
import com.wif3006.banking_system.dto.card.GetCardDto;
import com.wif3006.banking_system.dto.card.UpdateCardLimitDto;
import com.wif3006.banking_system.dto.card.UpdateCardPinDto;
import com.wif3006.banking_system.dto.card.UpdateCardStatusDto;
import com.wif3006.banking_system.service.CardService;

@RestController
@RequestMapping("/api/cards")
public class CardController {

    @Autowired
    private CardService cardService;

    @PostMapping("/create")
    public ResponseEntity<?> createCard(@RequestBody CreateCardDto request) {
        try {
            cardService.createCard(request);
            return ResponseEntity.ok("Card successfully created");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400).body("Request validation failed: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body("Operation conflict: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to create card: " + ex.getMessage());
        }
    }

    @PostMapping("/get")
    public ResponseEntity<?> getCard(@RequestBody GetCardDto request) {
        try {
            return ResponseEntity.ok(cardService.getCard(request));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400).body("Request validation failed: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Unable to retrieve card: " + ex.getMessage());
        }
    }

    @PatchMapping("/pin")
    public ResponseEntity<String> updatePin(@RequestBody UpdateCardPinDto request) {
        try {
            cardService.updateCardPin(request);
            return ResponseEntity.ok("Card PIN updated.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Request validation failed: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to update PIN: " + ex.getMessage());
        }
    }

    @PatchMapping("/limit")
    public ResponseEntity<String> updateLimit(@RequestBody UpdateCardLimitDto request) {
        try {
            cardService.updateCardTransactionLimit(request);
            return ResponseEntity.ok("Card transaction limit updated.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Request validation failed: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body("Operation conflict: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to update transaction limit: " + ex.getMessage());
        }
    }

    @PatchMapping("/status")
    public ResponseEntity<String> updateStatus(@RequestBody UpdateCardStatusDto request) {
        try {
            cardService.updateCardStatus(request);
            return ResponseEntity.ok("Card status updated.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body("Request validation failed: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body("Operation conflict: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500).body("Failed to update status: " + ex.getMessage());
        }
    }
}
