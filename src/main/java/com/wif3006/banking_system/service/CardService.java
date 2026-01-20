package com.wif3006.banking_system.service;

import com.wif3006.banking_system.dto.card.CardDto;
import com.wif3006.banking_system.dto.card.CreateCardDto;
import com.wif3006.banking_system.dto.card.GetCardDto;
import com.wif3006.banking_system.dto.card.UpdateCardLimitDto;
import com.wif3006.banking_system.dto.card.UpdateCardPinDto;
import com.wif3006.banking_system.dto.card.UpdateCardStatusDto;

public interface CardService {
    void createCard(CreateCardDto createCardDto);
    CardDto getCard(GetCardDto getCardDto);
    void updateCardPin(UpdateCardPinDto updateCardPinDto);
    void updateCardStatus(UpdateCardStatusDto updateCardStatusDto);
    void updateCardTransactionLimit(UpdateCardLimitDto updateCardLimitDto);
}
