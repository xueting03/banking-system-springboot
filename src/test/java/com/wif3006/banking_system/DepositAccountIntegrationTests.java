package com.wif3006.banking_system;

import java.math.BigDecimal;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.wif3006.banking_system.base.CustomerService;
import com.wif3006.banking_system.base.model.Customer;
import com.wif3006.banking_system.base.model.DepositAccount;
import com.wif3006.banking_system.customer.CustomerRepository;
import com.wif3006.banking_system.customer.dto.CreateCustomerDto;
import com.wif3006.banking_system.deposit.DepositAccountImplementation;
import com.wif3006.banking_system.deposit.DepositAccountRepository;
import com.wif3006.banking_system.deposit.dto.CreateDepositAccountDto;
import com.wif3006.banking_system.deposit.dto.DepositFundsDto;
import com.wif3006.banking_system.deposit.dto.GetDepositAccountDto;
import com.wif3006.banking_system.deposit.dto.UpdateDepositStatusDto;
import com.wif3006.banking_system.deposit.dto.WithdrawFundsDto;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class DepositAccountIntegrationTests {

    @Autowired
    private DepositAccountImplementation depositAccountImplementation;

    @Autowired
    private DepositAccountRepository depositAccountRepository;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    private static final String TEST_CUSTOMER_ID = "030119-08-3006";
    private static final String TEST_CUSTOMER_PASSWORD = "password123";
    private static final String TEST_CUSTOMER_NAME = "John Doe";
    private static final String TEST_CUSTOMER_PHONE = "0123456789";
    private static final String TEST_CUSTOMER_ADDRESS = "123 Main St";

    @BeforeEach
    public void setupTestCustomer() throws NoSuchAlgorithmException {
        // Create test customer
        CreateCustomerDto customerDto = new CreateCustomerDto();
        customerDto.setIdentificationNo(TEST_CUSTOMER_ID);
        customerDto.setPassword(TEST_CUSTOMER_PASSWORD);
        customerDto.setName(TEST_CUSTOMER_NAME);
        customerDto.setPhoneNo(TEST_CUSTOMER_PHONE);
        customerDto.setAddress(TEST_CUSTOMER_ADDRESS);
        customerService.createProfile(customerDto);
    }

    @Test
    public void testCreateDepositAccountPersistsAndIsRetrievable() {
        // Given
        CreateDepositAccountDto createDto = new CreateDepositAccountDto();
        createDto.setIdentificationNo(TEST_CUSTOMER_ID);
        createDto.setPassword(TEST_CUSTOMER_PASSWORD);
        createDto.setAmount(new BigDecimal("1000"));

        depositAccountImplementation.createAccount(createDto);

        // Verify persistence
        Optional<DepositAccount> savedAccount = depositAccountRepository.findByCustomerIdentificationNo(TEST_CUSTOMER_ID);
        assertTrue(savedAccount.isPresent(), "Account should be persisted in database");
        assertEquals(new BigDecimal("1000"), savedAccount.get().getAmount(), "Initial amount should match");
        assertEquals(DepositAccount.Status.ACTIVE, savedAccount.get().getStatus(), "Account should be ACTIVE");
        assertNotNull(savedAccount.get().getId(), "Account ID should be generated");
        assertNotNull(savedAccount.get().getCreatedAt(), "Created timestamp should be set");

        // Verify retrievability via service
        GetDepositAccountDto retrievedDto = depositAccountImplementation.getAccount(TEST_CUSTOMER_ID, TEST_CUSTOMER_PASSWORD);
        assertNotNull(retrievedDto, "Account should be retrievable");
        assertEquals(new BigDecimal("1000"), retrievedDto.getAmount(), "Retrieved amount should match");
        assertEquals("ACTIVE", retrievedDto.getStatus(), "Retrieved status should be ACTIVE");
    }

    @Test
    public void testDepositFundsUpdatesBalanceAndPersists() {
        // Create account with initial balance
        CreateDepositAccountDto createDto = new CreateDepositAccountDto();
        createDto.setIdentificationNo(TEST_CUSTOMER_ID);
        createDto.setPassword(TEST_CUSTOMER_PASSWORD);
        createDto.setAmount(new BigDecimal("1000"));
        depositAccountImplementation.createAccount(createDto);

        DepositFundsDto depositDto = new DepositFundsDto();
        depositDto.setIdentificationNo(TEST_CUSTOMER_ID);
        depositDto.setPassword(TEST_CUSTOMER_PASSWORD);
        depositDto.setAmount(new BigDecimal("500"));

        GetDepositAccountDto resultDto = depositAccountImplementation.depositFunds(depositDto);

        // Verify 
        assertEquals(new BigDecimal("1500"), resultDto.getAmount(), "Returned balance should be 1500");
        Optional<DepositAccount> updatedAccount = depositAccountRepository.findByCustomerIdentificationNo(TEST_CUSTOMER_ID);
        assertTrue(updatedAccount.isPresent(), "Account should exist in database");
        assertEquals(new BigDecimal("1500"), updatedAccount.get().getAmount(), "Persisted balance should be 1500");
    }

    @Test
    public void testWithdrawFundsUpdatesBalanceAndPersists() {
        // Create account with initial balance
        CreateDepositAccountDto createDto = new CreateDepositAccountDto();
        createDto.setIdentificationNo(TEST_CUSTOMER_ID);
        createDto.setPassword(TEST_CUSTOMER_PASSWORD);
        createDto.setAmount(new BigDecimal("1000"));
        depositAccountImplementation.createAccount(createDto);

        // Withdraw funds
        WithdrawFundsDto withdrawDto = new WithdrawFundsDto();
        withdrawDto.setIdentificationNo(TEST_CUSTOMER_ID);
        withdrawDto.setPassword(TEST_CUSTOMER_PASSWORD);
        withdrawDto.setAmount(new BigDecimal("300"));

        GetDepositAccountDto resultDto = depositAccountImplementation.withdrawFunds(withdrawDto);

        // Verify
        assertEquals(new BigDecimal("700"), resultDto.getAmount(), "Returned balance should be 700");
        Optional<DepositAccount> updatedAccount = depositAccountRepository.findByCustomerIdentificationNo(TEST_CUSTOMER_ID);
        assertTrue(updatedAccount.isPresent(), "Account should exist in database");
        assertEquals(new BigDecimal("700"), updatedAccount.get().getAmount(), "Persisted balance should be 700");
    }

    @Test
    public void testFreezeAccountPreventsOperations() {
        // Create and fund account
        CreateDepositAccountDto createDto = new CreateDepositAccountDto();
        createDto.setIdentificationNo(TEST_CUSTOMER_ID);
        createDto.setPassword(TEST_CUSTOMER_PASSWORD);
        createDto.setAmount(new BigDecimal("1000"));
        depositAccountImplementation.createAccount(createDto);

        // Freeze account
        UpdateDepositStatusDto freezeDto = new UpdateDepositStatusDto();
        freezeDto.setIdentificationNo(TEST_CUSTOMER_ID);
        freezeDto.setPassword(TEST_CUSTOMER_PASSWORD);
        freezeDto.setAction("FREEZE");

        GetDepositAccountDto frozenDto = depositAccountImplementation.updateStatus(freezeDto);

        // Verify status change is persisted
        assertEquals("FROZEN", frozenDto.getStatus(), "Returned status should be FROZEN");
        Optional<DepositAccount> frozenAccount = depositAccountRepository.findByCustomerIdentificationNo(TEST_CUSTOMER_ID);
        assertTrue(frozenAccount.isPresent(), "Account should exist");
        assertEquals(DepositAccount.Status.FROZEN, frozenAccount.get().getStatus(), "Persisted status should be FROZEN");

        // Verify deposit is blocked
        DepositFundsDto depositDto = new DepositFundsDto();
        depositDto.setIdentificationNo(TEST_CUSTOMER_ID);
        depositDto.setPassword(TEST_CUSTOMER_PASSWORD);
        depositDto.setAmount(new BigDecimal("100"));

        assertThrows(IllegalStateException.class, () -> {
            depositAccountImplementation.depositFunds(depositDto);
        }, "Deposit should throw exception for frozen account");

        // Verify withdraw is blocked
        WithdrawFundsDto withdrawDto = new WithdrawFundsDto();
        withdrawDto.setIdentificationNo(TEST_CUSTOMER_ID);
        withdrawDto.setPassword(TEST_CUSTOMER_PASSWORD);
        withdrawDto.setAmount(new BigDecimal("100"));

        assertThrows(IllegalStateException.class, () -> {
            depositAccountImplementation.withdrawFunds(withdrawDto);
        }, "Withdraw should throw exception for frozen account");

        // Verify balance remains unchanged
        Optional<DepositAccount> finalAccount = depositAccountRepository.findByCustomerIdentificationNo(TEST_CUSTOMER_ID);
        assertEquals(new BigDecimal("1000"), finalAccount.get().getAmount(), "Balance should remain unchanged at 1000");
    }

    @Test
    public void testCloseAccountPreventsAllOperations() {
        // Create account
        CreateDepositAccountDto createDto = new CreateDepositAccountDto();
        createDto.setIdentificationNo(TEST_CUSTOMER_ID);
        createDto.setPassword(TEST_CUSTOMER_PASSWORD);
        createDto.setAmount(new BigDecimal("1000"));
        depositAccountImplementation.createAccount(createDto);

        // Close account
        depositAccountImplementation.closeAccount(TEST_CUSTOMER_ID, TEST_CUSTOMER_PASSWORD);

        // Verify status change is persisted
        Optional<DepositAccount> closedAccount = depositAccountRepository.findByCustomerIdentificationNo(TEST_CUSTOMER_ID);
        assertTrue(closedAccount.isPresent(), "Account should exist");
        assertEquals(DepositAccount.Status.CLOSED, closedAccount.get().getStatus(), "Status should be CLOSED");

        // Verify deposit is blocked
        DepositFundsDto depositDto = new DepositFundsDto();
        depositDto.setIdentificationNo(TEST_CUSTOMER_ID);
        depositDto.setPassword(TEST_CUSTOMER_PASSWORD);
        depositDto.setAmount(new BigDecimal("100"));
        assertThrows(IllegalStateException.class, 
            () -> depositAccountImplementation.depositFunds(depositDto),
            "Deposit should be blocked on closed account");

        // Verify withdraw is blocked
        WithdrawFundsDto withdrawDto = new WithdrawFundsDto();
        withdrawDto.setIdentificationNo(TEST_CUSTOMER_ID);
        withdrawDto.setPassword(TEST_CUSTOMER_PASSWORD);
        withdrawDto.setAmount(new BigDecimal("100"));
        assertThrows(IllegalStateException.class, 
            () -> depositAccountImplementation.withdrawFunds(withdrawDto),
            "Withdraw should be blocked on closed account");

        // Verify freeze is blocked
        UpdateDepositStatusDto freezeDto = new UpdateDepositStatusDto();
        freezeDto.setIdentificationNo(TEST_CUSTOMER_ID);
        freezeDto.setPassword(TEST_CUSTOMER_PASSWORD);
        freezeDto.setAction("FREEZE");
        assertThrows(IllegalStateException.class, 
            () -> depositAccountImplementation.updateStatus(freezeDto),
            "Freeze should be blocked on closed account");

        // Verify double close is blocked
        assertThrows(IllegalStateException.class, 
            () -> depositAccountImplementation.closeAccount(TEST_CUSTOMER_ID, TEST_CUSTOMER_PASSWORD),
            "Closing already closed account should be blocked");
    }


    @Test
    public void testEndToEndAccountLifecycle() {
        // Create account
        CreateDepositAccountDto createDto = new CreateDepositAccountDto();
        createDto.setIdentificationNo(TEST_CUSTOMER_ID);
        createDto.setPassword(TEST_CUSTOMER_PASSWORD);
        createDto.setAmount(new BigDecimal("500"));
        depositAccountImplementation.createAccount(createDto);

        Optional<DepositAccount> account = depositAccountRepository.findByCustomerIdentificationNo(TEST_CUSTOMER_ID);
        assertTrue(account.isPresent(), "Account should be created");
        assertEquals(new BigDecimal("500"), account.get().getAmount(), "Initial balance should be 500");
        assertEquals(DepositAccount.Status.ACTIVE, account.get().getStatus(), "Status should be ACTIVE");

        // Deposit funds
        DepositFundsDto depositDto = new DepositFundsDto();
        depositDto.setIdentificationNo(TEST_CUSTOMER_ID);
        depositDto.setPassword(TEST_CUSTOMER_PASSWORD);
        depositDto.setAmount(new BigDecimal("300"));
        depositAccountImplementation.depositFunds(depositDto);

        account = depositAccountRepository.findByCustomerIdentificationNo(TEST_CUSTOMER_ID);
        assertEquals(new BigDecimal("800"), account.get().getAmount(), "Balance should be 800 after deposit");

        // Withdraw funds
        WithdrawFundsDto withdrawDto = new WithdrawFundsDto();
        withdrawDto.setIdentificationNo(TEST_CUSTOMER_ID);
        withdrawDto.setPassword(TEST_CUSTOMER_PASSWORD);
        withdrawDto.setAmount(new BigDecimal("200"));
        depositAccountImplementation.withdrawFunds(withdrawDto);

        account = depositAccountRepository.findByCustomerIdentificationNo(TEST_CUSTOMER_ID);
        assertEquals(new BigDecimal("600"), account.get().getAmount(), "Balance should be 600 after withdrawal");

        // Freeze account
        UpdateDepositStatusDto freezeDto = new UpdateDepositStatusDto();
        freezeDto.setIdentificationNo(TEST_CUSTOMER_ID);
        freezeDto.setPassword(TEST_CUSTOMER_PASSWORD);
        freezeDto.setAction("FREEZE");
        depositAccountImplementation.updateStatus(freezeDto);

        account = depositAccountRepository.findByCustomerIdentificationNo(TEST_CUSTOMER_ID);
        assertEquals(DepositAccount.Status.FROZEN, account.get().getStatus(), "Status should be FROZEN");

        // Unfreeze account
        UpdateDepositStatusDto unfreezeDto = new UpdateDepositStatusDto();
        unfreezeDto.setIdentificationNo(TEST_CUSTOMER_ID);
        unfreezeDto.setPassword(TEST_CUSTOMER_PASSWORD);
        unfreezeDto.setAction("UNFREEZE");
        depositAccountImplementation.updateStatus(unfreezeDto);

        account = depositAccountRepository.findByCustomerIdentificationNo(TEST_CUSTOMER_ID);
        assertEquals(DepositAccount.Status.ACTIVE, account.get().getStatus(), "Status should be ACTIVE again");

        // Close account
        depositAccountImplementation.closeAccount(TEST_CUSTOMER_ID, TEST_CUSTOMER_PASSWORD);

        account = depositAccountRepository.findByCustomerIdentificationNo(TEST_CUSTOMER_ID);
        assertEquals(DepositAccount.Status.CLOSED, account.get().getStatus(), "Status should be CLOSED");
        assertEquals(new BigDecimal("600"), account.get().getAmount(), "Balance should remain 600");
    }

    @Test
    public void testMultipleDepositsAccumulateCorrectly() {
        // Given
        CreateDepositAccountDto createDto = new CreateDepositAccountDto();
        createDto.setIdentificationNo(TEST_CUSTOMER_ID);
        createDto.setPassword(TEST_CUSTOMER_PASSWORD);
        createDto.setAmount(new BigDecimal("100"));
        depositAccountImplementation.createAccount(createDto);

        // Multiple deposits
        for (int i = 0; i < 5; i++) {
            DepositFundsDto depositDto = new DepositFundsDto();
            depositDto.setIdentificationNo(TEST_CUSTOMER_ID);
            depositDto.setPassword(TEST_CUSTOMER_PASSWORD);
            depositDto.setAmount(new BigDecimal("50"));
            depositAccountImplementation.depositFunds(depositDto);
        }
        Optional<DepositAccount> account = depositAccountRepository.findByCustomerIdentificationNo(TEST_CUSTOMER_ID);
        assertTrue(account.isPresent(), "Account should exist");
        assertEquals(new BigDecimal("350"), account.get().getAmount(), "Balance should be 100 + (5 * 50) = 350");
    }

    @Test
    public void testAccountRetrievalRequiresAuthentication() {
        // Given
        CreateDepositAccountDto createDto = new CreateDepositAccountDto();
        createDto.setIdentificationNo(TEST_CUSTOMER_ID);
        createDto.setPassword(TEST_CUSTOMER_PASSWORD);
        createDto.setAmount(new BigDecimal("1000"));
        depositAccountImplementation.createAccount(createDto);

        // Wrong password
        assertThrows(IllegalArgumentException.class, () -> {
            depositAccountImplementation.getAccount(TEST_CUSTOMER_ID, "wrongPassword");
        }, "Should throw exception for wrong password");
    }

    @Test
    public void testCustomerServiceAuthenticationIntegration() throws NoSuchAlgorithmException {
        // Create inactive customer
        String inactiveCustomerId = "040120-09-4007";
        CreateCustomerDto inactiveCustomerDto = new CreateCustomerDto();
        inactiveCustomerDto.setIdentificationNo(inactiveCustomerId);
        inactiveCustomerDto.setPassword(TEST_CUSTOMER_PASSWORD);
        inactiveCustomerDto.setName("Inactive User");
        inactiveCustomerDto.setPhoneNo("0198765432");
        inactiveCustomerDto.setAddress("456 Side St");
        customerService.createProfile(inactiveCustomerDto);

        // Make customer inactive
        Optional<Customer> customer = customerRepository.findByIdentificationNo(inactiveCustomerId);
        assertTrue(customer.isPresent(), "Customer should exist");
        customer.get().setStatus("INACTIVE");
        customerRepository.save(customer.get());

        // Verify login fails
        boolean loginResult = customerService.verifyLogin(inactiveCustomerId, TEST_CUSTOMER_PASSWORD);
        assertFalse(loginResult, "Login should fail for inactive customer");

        // Verify account creation fails
        CreateDepositAccountDto createDto = new CreateDepositAccountDto();
        createDto.setIdentificationNo(inactiveCustomerId);
        createDto.setPassword(TEST_CUSTOMER_PASSWORD);
        createDto.setAmount(new BigDecimal("1000"));

        assertThrows(IllegalArgumentException.class, () -> {
            depositAccountImplementation.createAccount(createDto);
        }, "Should throw exception for inactive customer");
    }

    @Test
    public void testMultipleOperationsMaintainCorrectFinalBalance() {
        // Given
        CreateDepositAccountDto createDto = new CreateDepositAccountDto();
        createDto.setIdentificationNo(TEST_CUSTOMER_ID);
        createDto.setPassword(TEST_CUSTOMER_PASSWORD);
        createDto.setAmount(new BigDecimal("1000"));
        depositAccountImplementation.createAccount(createDto);

        // Perform multiple operations
        DepositFundsDto depositDto = new DepositFundsDto();
        depositDto.setIdentificationNo(TEST_CUSTOMER_ID);
        depositDto.setPassword(TEST_CUSTOMER_PASSWORD);
        depositDto.setAmount(new BigDecimal("200"));
        depositAccountImplementation.depositFunds(depositDto);

        WithdrawFundsDto withdrawDto = new WithdrawFundsDto();
        withdrawDto.setIdentificationNo(TEST_CUSTOMER_ID);
        withdrawDto.setPassword(TEST_CUSTOMER_PASSWORD);
        withdrawDto.setAmount(new BigDecimal("150"));
        depositAccountImplementation.withdrawFunds(withdrawDto);

        // Verify final state
        Optional<DepositAccount> account = depositAccountRepository.findByCustomerIdentificationNo(TEST_CUSTOMER_ID);
        assertTrue(account.isPresent(), "Account should exist");
        assertEquals(new BigDecimal("1050"), account.get().getAmount(), "Final balance should be 1000 + 200 - 150 = 1050");
        assertEquals(DepositAccount.Status.ACTIVE, account.get().getStatus(), "Status should remain ACTIVE");
    }
}
