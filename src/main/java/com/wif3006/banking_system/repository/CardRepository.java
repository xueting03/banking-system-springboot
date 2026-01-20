package com.wif3006.banking_system.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wif3006.banking_system.model.Card;

public interface CardRepository extends JpaRepository<Card, UUID> {
    Optional<Card> findByAccountId(UUID accountId);
}
