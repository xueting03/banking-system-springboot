package com.wif3006.banking_system.service;

import com.wif3006.banking_system.model.Card;
import com.wif3006.banking_system.model.DepositAccount;
import com.wif3006.banking_system.repository.CardRepository;
import com.wif3006.banking_system.dto.card.*;
import com.wif3006.banking_system.dto.deposit.GetDepositAccountDto;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

@Service
public class CardImplementation implements CardService {
    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private DepositAccountService depositAccountService;

    private static final int DEFAULT_TRANSACTION_LIMIT = 5000;
    private static final int MAX_TRANSACTION_LIMIT = 10000;
    private static final int MIN_TRANSACTION_LIMIT = 100;

    @Override
    @Transactional
    public void createCard(CreateCardDto createCardDto) {
        verifyCustomerLogin(createCardDto.getIdentificationNo(), createCardDto.getPassword());
        GetDepositAccountDto depositAccountDto =
                depositAccountService.getAccount(createCardDto.getIdentificationNo(), createCardDto.getPassword());

        if (!depositAccountDto.getStatus().equalsIgnoreCase(DepositAccount.Status.ACTIVE.toString())) {
            throw new IllegalStateException("Deposit account must be ACTIVE to create a card.");
        }

        DepositAccount depositAccount = new DepositAccount();
        UUID accountId = UUID.fromString(depositAccountDto.getId());
        depositAccount.setId(accountId);

        Optional<Card> existingCard = cardRepository.findByAccountId(accountId);
        if (existingCard.isPresent()) {
            throw new IllegalStateException("Customer account already linked to a card.");
        }

        if (!isPinNumberValid(createCardDto.getPinNumber())) {
            throw new IllegalArgumentException("PIN number must be a 6-digit numeric string.");
        }

        Card card = new Card();
        card.setAccount(depositAccount);
        card.setCardNumber(generateCardNumber());
        card.setTransactionLimit(DEFAULT_TRANSACTION_LIMIT);
        card.setStatus(Card.CardStatus.INACTIVE);
        card.setPinNumber(createCardDto.getPinNumber());
        card.setCreatedAt(new Date());
        cardRepository.save(card);
    }

    @Override
    public CardDto getCard(GetCardDto getCardDto) {
        String identificationNo = getCardDto.getIdentificationNo();
        String password = getCardDto.getPassword();
        verifyCustomerLogin(identificationNo, password);
        Card card = getCardEntity(identificationNo, password);
        cardRepository.save(card);
        return CardDto.fromCard(card);
    }

    @Override
    @Transactional
    public void updateCardPin(UpdateCardPinDto updateCardPinDto) {
        String identificationNo = updateCardPinDto.getIdentificationNo();
        String password = updateCardPinDto.getPassword();

        verifyCustomerLogin(identificationNo, password);
        Card card = getCardEntity(identificationNo, password);

        if (card.getStatus() != Card.CardStatus.ACTIVE) {
            throw new IllegalStateException("Card is not in ACTIVE status and cannot update PIN.");
        }

        if (!card.getPinNumber().equals(updateCardPinDto.getCurrentPin())) {
            throw new IllegalArgumentException("PIN number is incorrect.");
        }
        if (!isPinNumberValid(updateCardPinDto.getNewPin())) {
            throw new IllegalArgumentException("PIN number must be a 6-digit numeric string.");
        }

        card.setPinNumber(updateCardPinDto.getNewPin());
        cardRepository.save(card);
    }

    @Override
    @Transactional
    public void updateCardTransactionLimit(UpdateCardLimitDto updateCardLimitDto) {
        String identificationNo = updateCardLimitDto.getIdentificationNo();
        String password = updateCardLimitDto.getPassword();

        verifyCustomerLogin(identificationNo, password);
        Card card = getCardEntity(identificationNo, password);

        if (!card.getPinNumber().equals(updateCardLimitDto.getPinNumber())) {
            throw new IllegalArgumentException("PIN number is incorrect.");
        }

        int newLimit = updateCardLimitDto.getNewLimit();
        if (newLimit <= MIN_TRANSACTION_LIMIT || newLimit > MAX_TRANSACTION_LIMIT) {
            throw new IllegalArgumentException("Transaction limit must be a positive integer not exceeding 10,000.");
        }
        card.setTransactionLimit(newLimit);
        cardRepository.save(card);
    }

    @Override
    @Transactional
    public void updateCardStatus(UpdateCardStatusDto updateCardStatusDto) {
        String identificationNo = updateCardStatusDto.getIdentificationNo();
        String password = updateCardStatusDto.getPassword();

        verifyCustomerLogin(identificationNo, password);
        Card card = getCardEntity(identificationNo, password);

        if (!card.getPinNumber().equals(updateCardStatusDto.getPinNumber())) {
            throw new IllegalArgumentException("PIN number is incorrect.");
        }

        GetDepositAccountDto depositAccountDto =
                depositAccountService.getAccount(updateCardStatusDto.getIdentificationNo(), updateCardStatusDto.getPassword());

        validateAndUpdateStatus(card, depositAccountDto, updateCardStatusDto.getAction());
        cardRepository.save(card);
    }

    /**
     * Helper method to verify customer login credentials.
     */
    private void verifyCustomerLogin(String identificationNo, String password) {
        if (!customerService.verifyLogin(identificationNo, password)) {
            throw new IllegalArgumentException("Invalid customer credentials.");
        }
    }

    /**
     * Helper method to get Card entity and sync its status with linked Deposit Account.
     */
    private Card getCardEntity(String identificationNo, String password) {
        GetDepositAccountDto depositAccountDto =
                depositAccountService.getAccount(identificationNo, password);
        Card card = cardRepository.findByAccountId(UUID.fromString(depositAccountDto.getId()))
                .orElseThrow(() -> new IllegalArgumentException("Customer does not have a card."));
        return syncCardStatus(card, depositAccountDto);
    }

    /**
     * Syncs the card status with the linked deposit account status.
     */
    private Card syncCardStatus(Card card, GetDepositAccountDto depositAccountDto) {
        DepositAccount.Status accountStatus = DepositAccount.Status.valueOf(depositAccountDto.getStatus());
        // if deposit account is frozen, freeze the card
        if (accountStatus == DepositAccount.Status.FROZEN && card.getStatus() != Card.CardStatus.FROZEN) {
            card.setStatus(Card.CardStatus.FROZEN);
        }
        // if deposit account is closed, deactivate the card
        else if (accountStatus == DepositAccount.Status.CLOSED && card.getStatus() != Card.CardStatus.INACTIVE) {
            card.setStatus(Card.CardStatus.INACTIVE);
        }
        return card;
    }

    /**
     * Generates a random 16-digit numeric card number.
     */
    private String generateCardNumber() {
        // generate 16-digit numeric card number
        StringBuilder sb = new StringBuilder();
        while (sb.length() < 16) {
            sb.append((int)(Math.random() * 10));
        }
        return sb.toString();
    }

    /**
     * Validates if the PIN number is a 6-digit numeric string.
     */
    private boolean isPinNumberValid(String pinNumber) {
        if (pinNumber == null) return false;
        return pinNumber.matches("\\d{6}");
    }

    /**
     * Validates and updates the card status based on the requested action
     * and the linked deposit account status.
     */
    private void validateAndUpdateStatus(Card card, GetDepositAccountDto depositAccountDto, UpdateCardStatusDto.Action action) {

        Card.CardStatus current = card.getStatus();
        DepositAccount.Status accountStatus = DepositAccount.Status.valueOf(depositAccountDto.getStatus());

        switch (action) {

            case ACTIVATE -> {
                if (accountStatus != DepositAccount.Status.ACTIVE) {
                    throw new IllegalStateException("Card can only be activated if linked deposit account is ACTIVE.");
                }
                if (current == Card.CardStatus.ACTIVE) {
                    throw new IllegalStateException("Card is already in ACTIVE status.");
                }
                if (current == Card.CardStatus.FROZEN) {
                    throw new IllegalStateException("Card is frozen. Please unfreeze the card instead of activating.");
                }
                card.setStatus(Card.CardStatus.ACTIVE);
            }

            case DEACTIVATE -> {
                if (current == Card.CardStatus.INACTIVE) {
                    throw new IllegalStateException("Card is already in INACTIVE status.");
                }
                if (current == Card.CardStatus.FROZEN) {
                    throw new IllegalStateException("Frozen card cannot be deactivated. Please unfreeze first.");
                }
                card.setStatus(Card.CardStatus.INACTIVE);
            }

            case FREEZE -> {
                if (current == Card.CardStatus.FROZEN) {
                    throw new IllegalStateException("Card is already frozen.");
                }
                if (current != Card.CardStatus.ACTIVE) {
                    throw new IllegalStateException("Only ACTIVE cards can be frozen.");
                }
                card.setStatus(Card.CardStatus.FROZEN);
            }

            case UNFREEZE -> {
                if (accountStatus != DepositAccount.Status.ACTIVE) {
                    throw new IllegalStateException("Card can only be unfrozen if linked deposit account is ACTIVE.");
                }
                if (current != Card.CardStatus.FROZEN) {
                    throw new IllegalStateException("Only FROZEN cards can be unfrozen.");
                }
                card.setStatus(Card.CardStatus.ACTIVE);
            }

            default -> {
                throw new IllegalArgumentException("Invalid action for updating card status.");
            }
        }
    }

}
