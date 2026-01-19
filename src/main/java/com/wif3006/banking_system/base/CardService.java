package com.wif3006.banking_system.base;

import com.wif3006.banking_system.card.dto.*;

public interface CardService {
    void createCard(CreateCardDto createCardDto);
    CardDto getCard(GetCardDto getCardDto);
    void updateCardPin(UpdateCardPinDto updateCardPinDto);
    void updateCardStatus(UpdateCardStatusDto updateCardStatusDto);
    void updateCardTransactionLimit(UpdateCardLimitDto updateCardLimitDto);
}
