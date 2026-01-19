package com.wif3006.banking_system.card;

import com.wif3006.banking_system.base.model.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
    Optional<Card> findByAccountId(UUID accountId);
}
