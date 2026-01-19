package com.wif3006.banking_system;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.wif3006.banking_system.base.CustomerService;
import com.wif3006.banking_system.base.model.Customer;
import com.wif3006.banking_system.base.model.DepositAccount;
import com.wif3006.banking_system.customer.dto.GetCustomerDto;
import com.wif3006.banking_system.deposit.DepositAccountImplementation;
import com.wif3006.banking_system.deposit.DepositAccountRepository;
import com.wif3006.banking_system.deposit.dto.CreateDepositAccountDto;
import com.wif3006.banking_system.deposit.dto.DepositFundsDto;
import com.wif3006.banking_system.deposit.dto.GetDepositAccountDto;
import com.wif3006.banking_system.deposit.dto.UpdateDepositStatusDto;
import com.wif3006.banking_system.deposit.dto.WithdrawFundsDto;

//Unit tests for DepositAccountImplementation
@ExtendWith(MockitoExtension.class)
public class DepositAccountImplementationTests {

    @Mock
    private DepositAccountRepository depositAccountRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private DepositAccountImplementation depositAccountImplementation;

    // Helper method to build customer entity
    private Customer buildCustomerEntity() {
        Customer cust = new Customer();
        cust.setId(UUID.randomUUID());
        return cust;
    }

    // Helper method to construct deposit account with specified properties
    private DepositAccount constructDepositAccount(BigDecimal balance, DepositAccount.Status accountState) {
        DepositAccount acct = new DepositAccount();
        acct.setId(UUID.randomUUID());
        acct.setCustomer(buildCustomerEntity());
        acct.setAmount(balance);
        acct.setStatus(accountState);
        acct.setCreatedAt(LocalDateTime.now());
        return acct;
    }

    // Helper to verify account status matches expected value
    private void assertAccountStatus(DepositAccount.Status expected, DepositAccount.Status actual) {
        assertEquals(expected, actual);
    }

    @Test
    public void testCreateAccount() {
        // Setup test data
        final String customerId = "030119-08-3006";
        final String userPass = "password";
        final BigDecimal initialBalance = new BigDecimal("1500");
        
        CreateDepositAccountDto requestDto = new CreateDepositAccountDto();
        requestDto.setIdentificationNo(customerId);
        requestDto.setPassword(userPass);
        requestDto.setAmount(initialBalance);

        GetCustomerDto profileData = new GetCustomerDto();
        profileData.setId(UUID.randomUUID());
        
        // Configure mock behaviors
        when(customerService.verifyLogin(customerId, userPass)).thenReturn(true);
        when(depositAccountRepository.findByCustomerIdentificationNo(customerId)).thenReturn(Optional.empty());
        when(customerService.getProfile(customerId)).thenReturn(profileData);

        // Execute operation
        depositAccountImplementation.createAccount(requestDto);

        // Validate repository interaction
        verify(depositAccountRepository, times(1)).save(any(DepositAccount.class));
    }

    @Test
    public void testCreateAccountWithExistingAccount() {
        // Arrange
        CreateDepositAccountDto createDepositAccountDto = new CreateDepositAccountDto();
        createDepositAccountDto.setIdentificationNo("030119-08-3006");
        createDepositAccountDto.setPassword("password");
        createDepositAccountDto.setAmount(new BigDecimal("1000"));

        when(customerService.verifyLogin("030119-08-3006", "password")).thenReturn(true);
        DepositAccount existingAccount = new DepositAccount();
        when(depositAccountRepository.findByCustomerIdentificationNo("030119-08-3006"))
                .thenReturn(Optional.of(existingAccount));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depositAccountImplementation.createAccount(createDepositAccountDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any(DepositAccount.class));
    }

    @Test
    public void testCreateAccountWithInvalidLogin() {
        // Arrange
        CreateDepositAccountDto createDepositAccountDto = new CreateDepositAccountDto();
        createDepositAccountDto.setIdentificationNo("030119-08-3006");
        createDepositAccountDto.setPassword("wrongPassword");
        createDepositAccountDto.setAmount(new BigDecimal("1000"));

        when(customerService.verifyLogin("030119-08-3006", "wrongPassword")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountImplementation.createAccount(createDepositAccountDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any(DepositAccount.class));
    }

    @Test
    public void testGetAccount() {
        // Prepare test scenario
        final String userId = "030119-08-3006";
        final String authPass = "password";
        final BigDecimal accountBalance = new BigDecimal("1000");

        DepositAccount existingAccount = constructDepositAccount(accountBalance, DepositAccount.Status.ACTIVE);

        // Mock repository and service responses
        when(depositAccountRepository.findByCustomerIdentificationNo(userId)).thenReturn(Optional.of(existingAccount));
        when(customerService.verifyLogin(userId, authPass)).thenReturn(true);

        // Perform retrieval operation
        GetDepositAccountDto resultDto = depositAccountImplementation.getAccount(userId, authPass);

        // Verify response data integrity
        assertNotNull(resultDto);
        assertEquals(existingAccount.getId().toString(), resultDto.getId());
        assertEquals(existingAccount.getCustomer().getId().toString(), resultDto.getCustomerId());
        assertEquals(accountBalance, resultDto.getAmount());
        assertEquals("ACTIVE", resultDto.getStatus());
        assertEquals(existingAccount.getCreatedAt(), resultDto.getCreatedAt());
    }

    @Test
    public void testGetAccountWithInvalidLogin() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "wrongPassword";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(new BigDecimal("1000"));
        account.setStatus(DepositAccount.Status.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountImplementation.getAccount(identificationNo, password);
        });
        assertNotNull(exception);
    }

    @Test
    public void testCloseAccount() {
        // Initialize test parameters
        final String customerRef = "030119-08-3006";
        final String credentials = "password";

        DepositAccount activeAccount = constructDepositAccount(new BigDecimal("1000"), DepositAccount.Status.ACTIVE);

        // Setup mock responses
        when(depositAccountRepository.findByCustomerIdentificationNo(customerRef)).thenReturn(Optional.of(activeAccount));
        when(customerService.verifyLogin(customerRef, credentials)).thenReturn(true);

        // Trigger closure operation
        depositAccountImplementation.closeAccount(customerRef, credentials);

        // Confirm account state updated correctly
        assertAccountStatus(DepositAccount.Status.CLOSED, activeAccount.getStatus());
        verify(depositAccountRepository, times(1)).save(activeAccount);
    }

    @Test
    public void testCloseAccountWithInvalidLogin() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "wrongPassword";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(new BigDecimal("1000"));
        account.setStatus(DepositAccount.Status.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountImplementation.closeAccount(identificationNo, password);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any(DepositAccount.class));
    }

    @Test
    public void testCloseAccountAlreadyClosed() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(new BigDecimal("1000"));
        account.setStatus(DepositAccount.Status.CLOSED);
        account.setCreatedAt(LocalDateTime.now());

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depositAccountImplementation.closeAccount(identificationNo, password);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any(DepositAccount.class));
    }

    // deposit fund
    @Test
    public void testDepositFundsSuccess() {
        // Define test constants
        final String accountRef = "030119-08-3006";
        final String authToken = "password";
        final BigDecimal startingBalance = new BigDecimal("1000");
        final BigDecimal depositValue = new BigDecimal("200");
        final BigDecimal expectedBalance = startingBalance.add(depositValue);

        DepositAccount targetAccount = constructDepositAccount(startingBalance, DepositAccount.Status.ACTIVE);

        DepositFundsDto transactionRequest = new DepositFundsDto();
        transactionRequest.setIdentificationNo(accountRef);
        transactionRequest.setPassword(authToken);
        transactionRequest.setAmount(depositValue);

        // Configure mocks
        when(depositAccountRepository.findByCustomerIdentificationNo(accountRef)).thenReturn(Optional.of(targetAccount));
        when(customerService.verifyLogin(accountRef, authToken)).thenReturn(true);

        // Execute deposit transaction
        GetDepositAccountDto transactionResult = depositAccountImplementation.depositFunds(transactionRequest);

        // Verify balance updated and response correct
        assertEquals(expectedBalance, targetAccount.getAmount());
        assertNotNull(transactionResult);
        assertEquals(expectedBalance, transactionResult.getAmount());
        assertEquals("ACTIVE", transactionResult.getStatus());
        verify(depositAccountRepository, times(1)).save(targetAccount);
    }

    @Test
    public void testDepositFundsInvalidLogin() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "wrongPassword";

        DepositFundsDto depositFundsDto = new DepositFundsDto();
        depositFundsDto.setIdentificationNo(identificationNo);
        depositFundsDto.setPassword(password);
        depositFundsDto.setAmount(new BigDecimal("200"));

        when(customerService.verifyLogin(identificationNo, password)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountImplementation.depositFunds(depositFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testDepositFundsNoAccountFound() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "password";

        DepositFundsDto depositFundsDto = new DepositFundsDto();
        depositFundsDto.setIdentificationNo(identificationNo);
        depositFundsDto.setPassword(password);
        depositFundsDto.setAmount(new BigDecimal("200"));

        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);
        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountImplementation.depositFunds(depositFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testDepositFundsClosedAccountNotAllowed() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(new BigDecimal("1000"));
        account.setStatus(DepositAccount.Status.CLOSED);
        account.setCreatedAt(LocalDateTime.now());

        DepositFundsDto depositFundsDto = new DepositFundsDto();
        depositFundsDto.setIdentificationNo(identificationNo);
        depositFundsDto.setPassword(password);
        depositFundsDto.setAmount(new BigDecimal("200"));

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depositAccountImplementation.depositFunds(depositFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testDepositFundsFrozenAccountNotAllowed() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(new BigDecimal("1000"));
        account.setStatus(DepositAccount.Status.FROZEN);
        account.setCreatedAt(LocalDateTime.now());

        DepositFundsDto depositFundsDto = new DepositFundsDto();
        depositFundsDto.setIdentificationNo(identificationNo);
        depositFundsDto.setPassword(password);
        depositFundsDto.setAmount(new BigDecimal("200"));

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depositAccountImplementation.depositFunds(depositFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testDepositFundsNonPositiveAmountRejected() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "password";

        DepositFundsDto depositFundsDto = new DepositFundsDto();
        depositFundsDto.setIdentificationNo(identificationNo);
        depositFundsDto.setPassword(password);
        depositFundsDto.setAmount(BigDecimal.ZERO);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountImplementation.depositFunds(depositFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    // withdraw fund
    @Test
    public void testWithdrawFundsSuccess() {
        // Test data setup
        final String userIdentifier = "030119-08-3006";
        final String userAuth = "password";
        final BigDecimal initialFunds = new BigDecimal("1000");
        final BigDecimal withdrawAmount = new BigDecimal("200");
        final BigDecimal remainingFunds = initialFunds.subtract(withdrawAmount);

        DepositAccount accountEntity = constructDepositAccount(initialFunds, DepositAccount.Status.ACTIVE);

        WithdrawFundsDto withdrawalRequest = new WithdrawFundsDto();
        withdrawalRequest.setIdentificationNo(userIdentifier);
        withdrawalRequest.setPassword(userAuth);
        withdrawalRequest.setAmount(withdrawAmount);

        // Mock setup
        when(depositAccountRepository.findByCustomerIdentificationNo(userIdentifier)).thenReturn(Optional.of(accountEntity));
        when(customerService.verifyLogin(userIdentifier, userAuth)).thenReturn(true);

        // Process withdrawal
        GetDepositAccountDto withdrawalResult = depositAccountImplementation.withdrawFunds(withdrawalRequest);

        // Validate outcome
        assertEquals(remainingFunds, accountEntity.getAmount());
        assertNotNull(withdrawalResult);
        assertEquals(remainingFunds, withdrawalResult.getAmount());
        assertEquals("ACTIVE", withdrawalResult.getStatus());
        verify(depositAccountRepository, times(1)).save(accountEntity);
    }

    @Test
    public void testWithdrawFundsInvalidLogin() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "wrongPassword";

        WithdrawFundsDto withdrawFundsDto = new WithdrawFundsDto();
        withdrawFundsDto.setIdentificationNo(identificationNo);
        withdrawFundsDto.setPassword(password);
        withdrawFundsDto.setAmount(new BigDecimal("200"));

        when(customerService.verifyLogin(identificationNo, password)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountImplementation.withdrawFunds(withdrawFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testWithdrawFundsInsufficientBalance() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(new BigDecimal("100"));
        account.setStatus(DepositAccount.Status.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());

        WithdrawFundsDto withdrawFundsDto = new WithdrawFundsDto();
        withdrawFundsDto.setIdentificationNo(identificationNo);
        withdrawFundsDto.setPassword(password);
        withdrawFundsDto.setAmount(new BigDecimal("200"));

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountImplementation.withdrawFunds(withdrawFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testWithdrawFundsClosedAccountNotAllowed() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(new BigDecimal("1000"));
        account.setStatus(DepositAccount.Status.CLOSED);
        account.setCreatedAt(LocalDateTime.now());

        WithdrawFundsDto withdrawFundsDto = new WithdrawFundsDto();
        withdrawFundsDto.setIdentificationNo(identificationNo);
        withdrawFundsDto.setPassword(password);
        withdrawFundsDto.setAmount(new BigDecimal("200"));

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depositAccountImplementation.withdrawFunds(withdrawFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testWithdrawFundsFrozenAccountNotAllowed() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(new BigDecimal("1000"));
        account.setStatus(DepositAccount.Status.FROZEN);
        account.setCreatedAt(LocalDateTime.now());

        WithdrawFundsDto withdrawFundsDto = new WithdrawFundsDto();
        withdrawFundsDto.setIdentificationNo(identificationNo);
        withdrawFundsDto.setPassword(password);
        withdrawFundsDto.setAmount(new BigDecimal("200"));

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depositAccountImplementation.withdrawFunds(withdrawFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testWithdrawFundsNonPositiveAmountRejected() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "password";

        WithdrawFundsDto withdrawFundsDto = new WithdrawFundsDto();
        withdrawFundsDto.setIdentificationNo(identificationNo);
        withdrawFundsDto.setPassword(password);
        withdrawFundsDto.setAmount(new BigDecimal("-100"));

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountImplementation.withdrawFunds(withdrawFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    // freeze or unfreeze account

    @Test
    public void testFreezeAccountSuccess() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(new BigDecimal("1000"));
        account.setStatus(DepositAccount.Status.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());

        UpdateDepositStatusDto updateStatusDto = new UpdateDepositStatusDto();
        updateStatusDto.setIdentificationNo(identificationNo);
        updateStatusDto.setPassword(password);
        updateStatusDto.setAction("FREEZE");

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act
        GetDepositAccountDto result = depositAccountImplementation.updateStatus(updateStatusDto);

        // Assert
        assertEquals(DepositAccount.Status.FROZEN, account.getStatus());
        assertNotNull(result);
        assertEquals("FROZEN", result.getStatus());
        verify(depositAccountRepository, times(1)).save(account);
    }

    @Test
    public void testUnfreezeAccountSuccess() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(new BigDecimal("1000"));
        account.setStatus(DepositAccount.Status.FROZEN);
        account.setCreatedAt(LocalDateTime.now());

        UpdateDepositStatusDto updateStatusDto = new UpdateDepositStatusDto();
        updateStatusDto.setIdentificationNo(identificationNo);
        updateStatusDto.setPassword(password);
        updateStatusDto.setAction("UNFREEZE");

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act
        GetDepositAccountDto result = depositAccountImplementation.updateStatus(updateStatusDto);

        // Assert
        assertEquals(DepositAccount.Status.ACTIVE, account.getStatus());
        assertNotNull(result);
        assertEquals("ACTIVE", result.getStatus());
        verify(depositAccountRepository, times(1)).save(account);
    }

    @Test
    public void testFreezeAccountAlreadyFrozen() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(new BigDecimal("1000"));
        account.setStatus(DepositAccount.Status.FROZEN);
        account.setCreatedAt(LocalDateTime.now());

        UpdateDepositStatusDto updateStatusDto = new UpdateDepositStatusDto();
        updateStatusDto.setIdentificationNo(identificationNo);
        updateStatusDto.setPassword(password);
        updateStatusDto.setAction("FREEZE");

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depositAccountImplementation.updateStatus(updateStatusDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testUnfreezeAccountWhenNotFrozen() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(new BigDecimal("1000"));
        account.setStatus(DepositAccount.Status.ACTIVE);
        account.setCreatedAt(LocalDateTime.now());

        UpdateDepositStatusDto updateStatusDto = new UpdateDepositStatusDto();
        updateStatusDto.setIdentificationNo(identificationNo);
        updateStatusDto.setPassword(password);
        updateStatusDto.setAction("UNFREEZE");

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depositAccountImplementation.updateStatus(updateStatusDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testUpdateStatusInvalidLogin() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "wrongPassword";

        UpdateDepositStatusDto updateStatusDto = new UpdateDepositStatusDto();
        updateStatusDto.setIdentificationNo(identificationNo);
        updateStatusDto.setPassword(password);
        updateStatusDto.setAction("FREEZE");

        when(customerService.verifyLogin(identificationNo, password)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountImplementation.updateStatus(updateStatusDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testUpdateStatusClosedAccountNotAllowed() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(new BigDecimal("1000"));
        account.setStatus(DepositAccount.Status.CLOSED);
        account.setCreatedAt(LocalDateTime.now());

        UpdateDepositStatusDto updateStatusDto = new UpdateDepositStatusDto();
        updateStatusDto.setIdentificationNo(identificationNo);
        updateStatusDto.setPassword(password);
        updateStatusDto.setAction("FREEZE");

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depositAccountImplementation.updateStatus(updateStatusDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testUpdateStatusInvalidAction() {
        // Arrange
        String identificationNo = "030119-08-3006";
        String password = "password";

        UpdateDepositStatusDto updateStatusDto = new UpdateDepositStatusDto();
        updateStatusDto.setIdentificationNo(identificationNo);
        updateStatusDto.setPassword(password);
        updateStatusDto.setAction("INVALID_ACTION");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountImplementation.updateStatus(updateStatusDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }
}