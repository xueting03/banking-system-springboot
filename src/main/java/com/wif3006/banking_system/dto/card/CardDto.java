package com.wif3006.banking_system.dto.card;

import com.wif3006.banking_system.model.Card;
import lombok.Data;

@Data
public class CardDto {
    private String cardNumber;
    private int transactionLimit;
    private String status;

    public static CardDto fromCard(Card card) {
        CardDto dto = new CardDto();
        dto.setCardNumber(card.getCardNumber());
        dto.setTransactionLimit(card.getTransactionLimit());
        dto.setStatus(card.getStatus().name());
        return dto;
    }
}
