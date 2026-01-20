package com.wif3006.banking_system;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.wif3006.banking_system.dto.card.CreateCardDto;
import com.wif3006.banking_system.dto.card.GetCardDto;
import com.wif3006.banking_system.dto.card.UpdateCardLimitDto;
import com.wif3006.banking_system.dto.card.UpdateCardPinDto;
import com.wif3006.banking_system.dto.card.UpdateCardStatusDto;
import com.wif3006.banking_system.dto.deposit.GetDepositAccountDto;
import com.wif3006.banking_system.model.Card;
import com.wif3006.banking_system.repository.CardRepository;
import com.wif3006.banking_system.service.CardImplementation;
import com.wif3006.banking_system.service.CustomerService;
import com.wif3006.banking_system.service.DepositAccountService;

public class CardImplementationTests {

    private CardImplementation cardService;
    private CardRepository cardRepository;
    private CustomerService customerService;
    private DepositAccountService depositAccountService;

    @BeforeEach
    public void setUp() throws Exception {
        cardRepository = Mockito.mock(CardRepository.class);
        customerService = Mockito.mock(CustomerService.class);
        depositAccountService = Mockito.mock(DepositAccountService.class);
        cardService = new CardImplementation();

        Field repoField = CardImplementation.class.getDeclaredField("cardRepository");
        repoField.setAccessible(true);
        repoField.set(cardService, cardRepository);

        Field custField = CardImplementation.class.getDeclaredField("customerService");
        custField.setAccessible(true);
        custField.set(cardService, customerService);

        Field depField = CardImplementation.class.getDeclaredField("depositAccountService");
        depField.setAccessible(true);
        depField.set(cardService, depositAccountService);
    }

    @Test
    public void testCreateCardSuccess() {
        String idNo = "010101-02-0303";
        String password = "password";
        String pin = "123456";

        // given valid card creation request
        CreateCardDto dto = new CreateCardDto();
        dto.setIdentificationNo(idNo);
        dto.setPassword(password);
        dto.setPinNumber(pin);

        Mockito.when(customerService.verifyLogin(idNo, password)).thenReturn(true);

        UUID accountId = UUID.randomUUID();
        GetDepositAccountDto depositDto = new GetDepositAccountDto();
        depositDto.setId(accountId.toString());
        depositDto.setStatus("ACTIVE");
        Mockito.when(depositAccountService.getAccount(idNo, password)).thenReturn(depositDto);

        Mockito.when(cardRepository.findByAccountId(accountId)).thenReturn(Optional.empty());

        // when trigger card creation
        cardService.createCard(dto);

        // then card is created and saved successfully
        Mockito.verify(cardRepository, Mockito.times(1)).save(Mockito.any(Card.class));
    }

    @Test
    public void testCreateCardDepositNotActive() {
        String idNo = "010101-02-0303";
        String password = "password";
        String pin = "123456";

        // given valid card creation request but deposit account not active
        CreateCardDto dto = new CreateCardDto();
        dto.setIdentificationNo(idNo);
        dto.setPassword(password);
        dto.setPinNumber(pin);

        Mockito.when(customerService.verifyLogin(idNo, password)).thenReturn(true);

        UUID accountId = UUID.randomUUID();
        GetDepositAccountDto depositDto = new GetDepositAccountDto();
        depositDto.setId(accountId.toString());
        depositDto.setStatus("FROZEN");
        Mockito.when(depositAccountService.getAccount(idNo, password)).thenReturn(depositDto);

        // when trigger card creation, then exception is thrown
        assertThrows(IllegalStateException.class, () -> cardService.createCard(dto));
    }

    @Test
    public void testCreateCardExistingCard() {
        String idNo = "010101-02-0303";
        String password = "password";
        String pin = "123456";

        // given valid card creation request but card already exists
        CreateCardDto dto = new CreateCardDto();
        dto.setIdentificationNo(idNo);
        dto.setPassword(password);
        dto.setPinNumber(pin);

        Mockito.when(customerService.verifyLogin(idNo, password)).thenReturn(true);

        UUID accountId = UUID.randomUUID();
        GetDepositAccountDto depositDto = new GetDepositAccountDto();
        depositDto.setId(accountId.toString());
        depositDto.setStatus("ACTIVE");
        Mockito.when(depositAccountService.getAccount(idNo, password)).thenReturn(depositDto);

        Card existing = new Card();
        existing.setId(UUID.randomUUID());
        Mockito.when(cardRepository.findByAccountId(accountId)).thenReturn(Optional.of(existing));

        // when trigger card creation, then exception is thrown
        assertThrows(IllegalStateException.class, () -> cardService.createCard(dto));
    }

    @Test
    public void testCreateCardInvalidPin() {
        String idNo = "010101-02-0303";
        String password = "password";

        // given invalid PIN format for card creation request
        CreateCardDto dto = new CreateCardDto();
        dto.setIdentificationNo(idNo);
        dto.setPassword(password);
        dto.setPinNumber("12AB"); // invalid PIN

        Mockito.when(customerService.verifyLogin(idNo, password)).thenReturn(true);

        UUID accountId = UUID.randomUUID();
        GetDepositAccountDto depositDto = new GetDepositAccountDto();
        depositDto.setId(accountId.toString());
        depositDto.setStatus("ACTIVE");
        Mockito.when(depositAccountService.getAccount(idNo, password)).thenReturn(depositDto);

        // when trigger card creation, then exception is thrown
        assertThrows(IllegalArgumentException.class, () -> cardService.createCard(dto));
    }

    @Test
    public void testGetCardSuccess() {
        String idNo = "010101-02-0303";
        String password = "password";

        // given valid get card request
        GetCardDto dto = new GetCardDto();
        dto.setIdentificationNo(idNo);
        dto.setPassword(password);

        Mockito.when(customerService.verifyLogin(idNo, password)).thenReturn(true);

        UUID accountId = UUID.randomUUID();
        GetDepositAccountDto depositDto = new GetDepositAccountDto();
        depositDto.setId(accountId.toString());
        depositDto.setStatus("ACTIVE");
        Mockito.when(depositAccountService.getAccount(idNo, password)).thenReturn(depositDto);

        Card card = new Card();
        card.setId(UUID.randomUUID());
        card.setAccount(null);
        card.setCardNumber("1111222233334444");
        card.setStatus(Card.CardStatus.ACTIVE);
        Mockito.when(cardRepository.findByAccountId(accountId)).thenReturn(Optional.of(card));

        // when trigger card retrieval
        var result = cardService.getCard(dto);

        // then card details returned
        assertNotNull(result);
        Mockito.verify(cardRepository).save(card);
    }

    @Test
    public void testGetCardInvalidCredentials() {
        String idNo = "010101-02-0303";
        String wrongPassword = "wrongPassword";

        // given invalid login credentials for get card request
        GetCardDto dto = new GetCardDto();
        dto.setIdentificationNo(idNo);
        dto.setPassword(wrongPassword);

        Mockito.when(customerService.verifyLogin(idNo, wrongPassword)).thenReturn(false);

        // when trigger card retrieval, then exception is thrown
        assertThrows(IllegalArgumentException.class, () -> cardService.getCard(dto));

        Mockito.verify(depositAccountService, Mockito.never()).getAccount(Mockito.anyString(), Mockito.anyString());
        Mockito.verify(cardRepository, Mockito.never()).findByAccountId(Mockito.any());
    }

    @Test
    public void testUpdateCardPinSuccess() {
        String idNo = "010101-02-0303";
        String password = "password";
        String pin = "123456";

        // given valid update PIN request
        UpdateCardPinDto dto = new UpdateCardPinDto();
        dto.setIdentificationNo(idNo);
        dto.setPassword(password);
        dto.setCurrentPin(pin);
        dto.setNewPin("111111");

        Mockito.when(customerService.verifyLogin(idNo, password)).thenReturn(true);

        UUID accountId = UUID.randomUUID();
        GetDepositAccountDto depositDto = new GetDepositAccountDto();
        depositDto.setId(accountId.toString());
        depositDto.setStatus("ACTIVE");
        Mockito.when(depositAccountService.getAccount(idNo, password)).thenReturn(depositDto);

        Card card = new Card();
        card.setStatus(Card.CardStatus.ACTIVE);
        card.setPinNumber(pin);
        Mockito.when(cardRepository.findByAccountId(accountId)).thenReturn(Optional.of(card));

        // when trigger PIN update
        cardService.updateCardPin(dto);

        // then PIN is updated successfully
        Mockito.verify(cardRepository).save(card);
        assertEquals("111111", card.getPinNumber());
    }

    @Test
    public void testUpdateCardPinWrongCurrent() {
        String idNo = "010101-02-0303";
        String password = "password";
        String pin = "123456";

        // given incorrect current PIN in update PIN request
        UpdateCardPinDto dto = new UpdateCardPinDto();
        dto.setIdentificationNo(idNo);
        dto.setPassword(password);
        dto.setCurrentPin("999999"); // wrong current PIN
        dto.setNewPin("111111");

        Mockito.when(customerService.verifyLogin(idNo, password)).thenReturn(true);

        UUID accountId = UUID.randomUUID();
        GetDepositAccountDto depositDto = new GetDepositAccountDto();
        depositDto.setId(accountId.toString());
        depositDto.setStatus("ACTIVE");
        Mockito.when(depositAccountService.getAccount(idNo, password)).thenReturn(depositDto);

        Card card = new Card();
        card.setStatus(Card.CardStatus.ACTIVE);
        card.setPinNumber(pin);
        Mockito.when(cardRepository.findByAccountId(accountId)).thenReturn(Optional.of(card));

        // when trigger PIN update, then exception is thrown
        assertThrows(IllegalArgumentException.class, () -> cardService.updateCardPin(dto));
    }

    @Test
    public void testUpdateCardPinInactiveCard() {
        String idNo = "010101-02-0303";
        String password = "password";
        String pin = "123456";

        // given inactive card in update PIN request
        UpdateCardPinDto dto = new UpdateCardPinDto();
        dto.setIdentificationNo(idNo);
        dto.setPassword(password);
        dto.setCurrentPin(pin);
        dto.setNewPin("111111");

        Mockito.when(customerService.verifyLogin(idNo, password)).thenReturn(true);

        UUID accountId = UUID.randomUUID();
        GetDepositAccountDto depositDto = new GetDepositAccountDto();
        depositDto.setId(accountId.toString());
        depositDto.setStatus("ACTIVE");
        Mockito.when(depositAccountService.getAccount(idNo, password)).thenReturn(depositDto);

        Card card = new Card();
        card.setStatus(Card.CardStatus.INACTIVE);
        card.setPinNumber(pin);
        Mockito.when(cardRepository.findByAccountId(accountId)).thenReturn(Optional.of(card));

        // when trigger PIN update, then exception is thrown
        assertThrows(IllegalStateException.class, () -> cardService.updateCardPin(dto));
    }

    @Test
    public void testUpdateTransactionLimitSuccess() {
        String idNo = "010101-02-0303";
        String password = "password";
        String pin = "123456";

        // given valid update transaction limit request
        UpdateCardLimitDto dto = new UpdateCardLimitDto();
        dto.setIdentificationNo(idNo);
        dto.setPassword(password);
        dto.setPinNumber(pin);
        dto.setNewLimit(3000);

        Mockito.when(customerService.verifyLogin(idNo, password)).thenReturn(true);

        UUID accountId = UUID.randomUUID();
        GetDepositAccountDto depositDto = new GetDepositAccountDto();
        depositDto.setId(accountId.toString());
        depositDto.setStatus("ACTIVE");
        Mockito.when(depositAccountService.getAccount(idNo, password)).thenReturn(depositDto);

        Card card = new Card();
        card.setPinNumber(pin);
        card.setStatus(Card.CardStatus.ACTIVE);
        card.setTransactionLimit(1000);
        Mockito.when(cardRepository.findByAccountId(accountId)).thenReturn(Optional.of(card));

        // when trigger transaction limit update
        cardService.updateCardTransactionLimit(dto);

        // then transaction limit is updated successfully
        Mockito.verify(cardRepository).save(card);
        assertEquals(3000, card.getTransactionLimit());
    }

    @Test
    public void testUpdateTransactionLimitInvalid() {
        String idNo = "010101-02-0303";
        String password = "password";
        String pin = "123456";

        // given invalid transaction limit in update request
        UpdateCardLimitDto dto = new UpdateCardLimitDto();
        dto.setIdentificationNo(idNo);
        dto.setPassword(password);
        dto.setPinNumber(pin);
        dto.setNewLimit(20000); // invalid limit

        Mockito.when(customerService.verifyLogin(idNo, password)).thenReturn(true);

        UUID accountId = UUID.randomUUID();
        GetDepositAccountDto depositDto = new GetDepositAccountDto();
        depositDto.setId(accountId.toString());
        depositDto.setStatus("ACTIVE");
        Mockito.when(depositAccountService.getAccount(idNo, password)).thenReturn(depositDto);

        Card card = new Card();
        card.setPinNumber(pin);
        card.setStatus(Card.CardStatus.ACTIVE);
        Mockito.when(cardRepository.findByAccountId(accountId)).thenReturn(Optional.of(card));

        // when trigger transaction limit update, then exception is thrown
        assertThrows(IllegalArgumentException.class, () -> cardService.updateCardTransactionLimit(dto));
    }

    @Test
    public void testUpdateTransactionLimitInactiveCard() {
        String idNo = "010101-02-0303";
        String password = "password";
        String pin = "123456";

        // given valid update transaction limit request but account is inactive
        UpdateCardLimitDto dto = new UpdateCardLimitDto();
        dto.setIdentificationNo(idNo);
        dto.setPassword(password);
        dto.setPinNumber(pin);
        dto.setNewLimit(3000);

        Mockito.when(customerService.verifyLogin(idNo, password)).thenReturn(true);

        UUID accountId = UUID.randomUUID();
        GetDepositAccountDto depositDto = new GetDepositAccountDto();
        depositDto.setId(accountId.toString());
        depositDto.setStatus("ACTIVE");
        Mockito.when(depositAccountService.getAccount(idNo, password)).thenReturn(depositDto);

        Card card = new Card();
        card.setPinNumber(pin);
        card.setStatus(Card.CardStatus.INACTIVE);
        card.setTransactionLimit(1000);
        Mockito.when(cardRepository.findByAccountId(accountId)).thenReturn(Optional.of(card));

        // when trigger transaction limit update then exception is thrown
        assertThrows(IllegalStateException.class, () -> cardService.updateCardTransactionLimit(dto));
    }

    @Test
    public void testUpdateCardStatusActivateSuccess() {
        String idNo = "010101-02-0303";
        String password = "password";
        String pin = "123456";

        // given valid activate card request
        UpdateCardStatusDto dto = new UpdateCardStatusDto();
        dto.setIdentificationNo(idNo);
        dto.setPassword(password);
        dto.setPinNumber(pin);
        dto.setAction(UpdateCardStatusDto.Action.ACTIVATE);

        Mockito.when(customerService.verifyLogin(idNo, password)).thenReturn(true);

        UUID accountId = UUID.randomUUID();
        GetDepositAccountDto depositDto = new GetDepositAccountDto();
        depositDto.setId(accountId.toString());
        depositDto.setStatus("ACTIVE");
        Mockito.when(depositAccountService.getAccount(idNo, password)).thenReturn(depositDto);

        Card card = new Card();
        card.setStatus(Card.CardStatus.INACTIVE);
        card.setPinNumber(pin);
        Mockito.when(cardRepository.findByAccountId(accountId)).thenReturn(Optional.of(card));

        // when trigger card activation
        cardService.updateCardStatus(dto);

        // then card is activated successfully
        Mockito.verify(cardRepository).save(card);
        assertEquals(Card.CardStatus.ACTIVE, card.getStatus());
    }

    @Test
    public void testUpdateCardStatusActivateAccountNotActive() {
        String idNo = "010101-02-0303";
        String password = "password";
        String pin = "123456";

        // given activate card request but deposit account not active
        UpdateCardStatusDto dto = new UpdateCardStatusDto();
        dto.setIdentificationNo(idNo);
        dto.setPassword(password);
        dto.setPinNumber(pin);
        dto.setAction(UpdateCardStatusDto.Action.ACTIVATE);

        Mockito.when(customerService.verifyLogin(idNo, password)).thenReturn(true);

        UUID accountId = UUID.randomUUID();
        GetDepositAccountDto depositDto = new GetDepositAccountDto();
        depositDto.setId(accountId.toString());
        depositDto.setStatus("CLOSED");
        Mockito.when(depositAccountService.getAccount(idNo, password)).thenReturn(depositDto);

        Card card = new Card();
        card.setStatus(Card.CardStatus.INACTIVE);
        card.setPinNumber(pin);
        Mockito.when(cardRepository.findByAccountId(accountId)).thenReturn(Optional.of(card));

        // when trigger card activation, then exception is thrown
        assertThrows(IllegalStateException.class, () -> cardService.updateCardStatus(dto));
    }

    @Test
    public void testUpdateCardStatusFreezeAlreadyFrozen() {
        String idNo = "010101-02-0303";
        String password = "password";
        String pin = "123456";

        // given freeze request but card already frozen
        UpdateCardStatusDto dto = new UpdateCardStatusDto();
        dto.setIdentificationNo(idNo);
        dto.setPassword(password);
        dto.setPinNumber(pin);
        dto.setAction(UpdateCardStatusDto.Action.FREEZE);

        Mockito.when(customerService.verifyLogin(idNo, password)).thenReturn(true);

        UUID accountId = UUID.randomUUID();
        GetDepositAccountDto depositDto = new GetDepositAccountDto();
        depositDto.setId(accountId.toString());
        depositDto.setStatus("ACTIVE");
        Mockito.when(depositAccountService.getAccount(idNo, password)).thenReturn(depositDto);

        Card card = new Card();
        card.setStatus(Card.CardStatus.FROZEN);
        card.setPinNumber(pin);
        Mockito.when(cardRepository.findByAccountId(accountId)).thenReturn(Optional.of(card));

        // when trigger freeze, then exception is thrown
        assertThrows(IllegalStateException.class, () -> cardService.updateCardStatus(dto));
    }

}
