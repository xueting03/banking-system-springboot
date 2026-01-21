package com.wif3006.banking_system;

import com.wif3006.banking_system.dto.card.*;
import com.wif3006.banking_system.dto.customer.CreateCustomerDto;
import com.wif3006.banking_system.dto.deposit.CreateDepositAccountDto;
import com.wif3006.banking_system.dto.deposit.UpdateDepositStatusDto;
import com.wif3006.banking_system.model.Card;
import com.wif3006.banking_system.model.DepositAccount;
import com.wif3006.banking_system.repository.CardRepository;
import com.wif3006.banking_system.repository.DepositAccountRepository;
import com.wif3006.banking_system.service.CardImplementation;
import com.wif3006.banking_system.service.CustomerService;
import com.wif3006.banking_system.service.DepositAccountImplementation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class CardIntegrationTests {

    @Autowired
    private CardImplementation cardImplementation;

    @Autowired
    private CardRepository cardRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private DepositAccountImplementation depositAccountImplementation;

    @Autowired
    private DepositAccountRepository depositAccountRepository;

    private static final String TEST_CUSTOMER_ID = "010101-02-0303";
    private static final String TEST_CUSTOMER_PASSWORD = "password123";
    private static final String TEST_CUSTOMER_NAME = "Jane Doe";
    private static final String TEST_CUSTOMER_PHONE = "0112233445";
    private static final String TEST_CUSTOMER_ADDRESS = "321 Side St";

    @BeforeEach
    public void setupCustomer() throws NoSuchAlgorithmException {
        CreateCustomerDto dto = new CreateCustomerDto();
        dto.setIdentificationNo(TEST_CUSTOMER_ID);
        dto.setPassword(TEST_CUSTOMER_PASSWORD);
        dto.setName(TEST_CUSTOMER_NAME);
        dto.setPhoneNo(TEST_CUSTOMER_PHONE);
        dto.setAddress(TEST_CUSTOMER_ADDRESS);
        customerService.createProfile(dto);
    }

    @Test
    public void testCreateCardPersistsAndRetrievable() {
        // given deposit account exists
        CreateDepositAccountDto createDeposit = new CreateDepositAccountDto();
        createDeposit.setIdentificationNo(TEST_CUSTOMER_ID);
        createDeposit.setPassword(TEST_CUSTOMER_PASSWORD);
        createDeposit.setAmount(new BigDecimal("1000"));
        depositAccountImplementation.createAccount(createDeposit);

        Optional<DepositAccount> account = depositAccountRepository.findByCustomerIdentificationNo(TEST_CUSTOMER_ID);
        assertTrue(account.isPresent());
        UUID accountId = account.get().getId();

        // when trigger card creation
        CreateCardDto createCardDto = new CreateCardDto();
        createCardDto.setIdentificationNo(TEST_CUSTOMER_ID);
        createCardDto.setPassword(TEST_CUSTOMER_PASSWORD);
        createCardDto.setPinNumber("123456");
        cardImplementation.createCard(createCardDto);

        // then card is persisted correctly
        Optional<Card> cardOpt = cardRepository.findByAccountId(accountId);
        assertTrue(cardOpt.isPresent());
        Card card = cardOpt.get();

        assertNotNull(card.getCardNumber());
        assertEquals(16, card.getCardNumber().length());
        assertEquals("123456", card.getPinNumber());
        assertEquals(Card.CardStatus.INACTIVE, card.getStatus());
        assertEquals(5000, card.getTransactionLimit()); // default
    }

    @Test
    public void testCreateCardBlockedWhenDepositFrozen() {
        // given deposit account is frozen
        CreateDepositAccountDto createDeposit = new CreateDepositAccountDto();
        createDeposit.setIdentificationNo(TEST_CUSTOMER_ID);
        createDeposit.setPassword(TEST_CUSTOMER_PASSWORD);
        createDeposit.setAmount(new BigDecimal("500"));
        depositAccountImplementation.createAccount(createDeposit);

        depositAccountImplementation.updateStatus(new UpdateDepositStatusDto() {{
            setIdentificationNo(TEST_CUSTOMER_ID);
            setPassword(TEST_CUSTOMER_PASSWORD);
            setAction("FREEZE");
        }});

        // when trigger card creation, should throw
        CreateCardDto createCardDto = new CreateCardDto();
        createCardDto.setIdentificationNo(TEST_CUSTOMER_ID);
        createCardDto.setPassword(TEST_CUSTOMER_PASSWORD);
        createCardDto.setPinNumber("654321");

        assertThrows(IllegalStateException.class, () -> cardImplementation.createCard(createCardDto));
    }

    @Test
    public void testCreateCardDuplicateBlocked() {
        // given deposit account already linked with existing card
        CreateDepositAccountDto createDeposit = new CreateDepositAccountDto();
        createDeposit.setIdentificationNo(TEST_CUSTOMER_ID);
        createDeposit.setPassword(TEST_CUSTOMER_PASSWORD);
        createDeposit.setAmount(new BigDecimal("800"));
        depositAccountImplementation.createAccount(createDeposit);

        CreateCardDto createCardDto = new CreateCardDto();
        createCardDto.setIdentificationNo(TEST_CUSTOMER_ID);
        createCardDto.setPassword(TEST_CUSTOMER_PASSWORD);
        createCardDto.setPinNumber("000111");
        cardImplementation.createCard(createCardDto);

        // when trigger duplicate card creation, should throw
        assertThrows(IllegalStateException.class, () -> cardImplementation.createCard(createCardDto));
    }

    @Test
    public void testGetCardSyncsStatusOnDepositStateChanges() {
        // given deposit account and card exist
        CreateDepositAccountDto createDeposit = new CreateDepositAccountDto();
        createDeposit.setIdentificationNo(TEST_CUSTOMER_ID);
        createDeposit.setPassword(TEST_CUSTOMER_PASSWORD);
        createDeposit.setAmount(new BigDecimal("1200"));
        depositAccountImplementation.createAccount(createDeposit);

        CreateCardDto createCardDto = new CreateCardDto();
        createCardDto.setIdentificationNo(TEST_CUSTOMER_ID);
        createCardDto.setPassword(TEST_CUSTOMER_PASSWORD);
        createCardDto.setPinNumber("111222");
        cardImplementation.createCard(createCardDto);

        // when deposit account is frozen
        UpdateDepositStatusDto freezeDto = new UpdateDepositStatusDto();
        freezeDto.setIdentificationNo(TEST_CUSTOMER_ID);
        freezeDto.setPassword(TEST_CUSTOMER_PASSWORD);
        freezeDto.setAction("FREEZE");
        depositAccountImplementation.updateStatus(freezeDto);

        // then card status updated to FROZEN
        GetCardDto getCardDto = new GetCardDto();
        getCardDto.setIdentificationNo(TEST_CUSTOMER_ID);
        getCardDto.setPassword(TEST_CUSTOMER_PASSWORD);
        var cardDto = cardImplementation.getCard(getCardDto);
        assertEquals("FROZEN", cardDto.getStatus());

        // when deposit account is close
        depositAccountImplementation.closeAccount(TEST_CUSTOMER_ID, TEST_CUSTOMER_PASSWORD);

        // then card status updated to INACTIVE
        cardDto = cardImplementation.getCard(getCardDto);
        assertEquals("INACTIVE", cardDto.getStatus());
    }

    @Test
    public void testActivateCardUpdatePinAndLimitFlows() {
        // create deposit account and card
        CreateDepositAccountDto createDeposit = new CreateDepositAccountDto();
        createDeposit.setIdentificationNo(TEST_CUSTOMER_ID);
        createDeposit.setPassword(TEST_CUSTOMER_PASSWORD);
        createDeposit.setAmount(new BigDecimal("1000"));
        depositAccountImplementation.createAccount(createDeposit);

        CreateCardDto createCardDto = new CreateCardDto();
        createCardDto.setIdentificationNo(TEST_CUSTOMER_ID);
        createCardDto.setPassword(TEST_CUSTOMER_PASSWORD);
        createCardDto.setPinNumber("222333");
        cardImplementation.createCard(createCardDto);

        // activate card
        UpdateCardStatusDto activate = new UpdateCardStatusDto();
        activate.setIdentificationNo(TEST_CUSTOMER_ID);
        activate.setPassword(TEST_CUSTOMER_PASSWORD);
        activate.setPinNumber("222333");
        activate.setAction(UpdateCardStatusDto.Action.ACTIVATE);
        cardImplementation.updateCardStatus(activate);

        // update PIN
        UpdateCardPinDto pinDto = new UpdateCardPinDto();
        pinDto.setIdentificationNo(TEST_CUSTOMER_ID);
        pinDto.setPassword(TEST_CUSTOMER_PASSWORD);
        pinDto.setCurrentPin("222333");
        pinDto.setNewPin("999888");
        cardImplementation.updateCardPin(pinDto);

        // verify pin updated in repository
        Optional<DepositAccount> account = depositAccountRepository.findByCustomerIdentificationNo(TEST_CUSTOMER_ID);
        assertTrue(account.isPresent());
        UUID accountId = account.get().getId();
        Card card = cardRepository.findByAccountId(accountId).orElseThrow();
        assertEquals("999888", card.getPinNumber());

        // update transaction limit valid
        UpdateCardLimitDto limitDto = new UpdateCardLimitDto();
        limitDto.setIdentificationNo(TEST_CUSTOMER_ID);
        limitDto.setPassword(TEST_CUSTOMER_PASSWORD);
        limitDto.setPinNumber("999888");
        limitDto.setNewLimit(3000);
        cardImplementation.updateCardTransactionLimit(limitDto);

        card = cardRepository.findByAccountId(accountId).orElseThrow();
        assertEquals(3000, card.getTransactionLimit());

        // invalid new limit should throw
        limitDto.setNewLimit(20000);
        assertThrows(IllegalArgumentException.class, () -> cardImplementation.updateCardTransactionLimit(limitDto));
    }

    @Test
    public void testActivateBlockedWhenDepositNotActive() {
        // create deposit account then close it
        CreateDepositAccountDto createDeposit = new CreateDepositAccountDto();
        createDeposit.setIdentificationNo(TEST_CUSTOMER_ID);
        createDeposit.setPassword(TEST_CUSTOMER_PASSWORD);
        createDeposit.setAmount(new BigDecimal("600"));
        depositAccountImplementation.createAccount(createDeposit);

        CreateCardDto createCardDto = new CreateCardDto();
        createCardDto.setIdentificationNo(TEST_CUSTOMER_ID);
        createCardDto.setPassword(TEST_CUSTOMER_PASSWORD);
        createCardDto.setPinNumber("333444");
        cardImplementation.createCard(createCardDto);

        // close deposit account
        depositAccountImplementation.closeAccount(TEST_CUSTOMER_ID, TEST_CUSTOMER_PASSWORD);

        UpdateCardStatusDto activate = new UpdateCardStatusDto();
        activate.setIdentificationNo(TEST_CUSTOMER_ID);
        activate.setPassword(TEST_CUSTOMER_PASSWORD);
        activate.setPinNumber("333444");
        activate.setAction(UpdateCardStatusDto.Action.ACTIVATE);

        assertThrows(IllegalStateException.class, () -> cardImplementation.updateCardStatus(activate));
    }
}
