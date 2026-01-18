package com.wif3006.banking_system;

import java.util.Date;
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
import com.wif3006.banking_system.deposit.DepositAccountManagement;
import com.wif3006.banking_system.deposit.DepositAccountRepository;
import com.wif3006.banking_system.deposit.dto.CreateDepositAccountDto;
import com.wif3006.banking_system.deposit.dto.DepositFundsDto;
import com.wif3006.banking_system.deposit.dto.GetDepositAccountDto;
import com.wif3006.banking_system.deposit.dto.UpdateDepositStatusDto;
import com.wif3006.banking_system.deposit.dto.WithdrawFundsDto;

/**
 * Unit tests for DepositAccountManagement
 * Tests service logic in isolation with mocked dependencies
 */
@ExtendWith(MockitoExtension.class)
public class DepositAccountManagementTests {

    @Mock
    private DepositAccountRepository depositAccountRepository;

    @Mock
    private CustomerService customerService;

    @InjectMocks
    private DepositAccountManagement depositAccountManagement;

    @Test
    public void testCreateAccount() {
        // Arrange
        CreateDepositAccountDto createDepositAccountDto = new CreateDepositAccountDto();
        createDepositAccountDto.setIdentificationNo("021023-08-1925");
        createDepositAccountDto.setPassword("password");
        createDepositAccountDto.setAmount(1000);

        when(customerService.verifyLogin("021023-08-1925", "password")).thenReturn(true);
        when(depositAccountRepository.findByCustomerIdentificationNo("021023-08-1925"))
                .thenReturn(Optional.empty());

        GetCustomerDto customerDto = new GetCustomerDto();
        customerDto.setId(UUID.randomUUID());
        when(customerService.getProfile("021023-08-1925")).thenReturn(customerDto);

        // Act
        depositAccountManagement.createAccount(createDepositAccountDto);

        // Assert
        verify(depositAccountRepository, times(1)).save(any(DepositAccount.class));
    }

    @Test
    public void testCreateAccountWithExistingAccount() {
        // Arrange
        CreateDepositAccountDto createDepositAccountDto = new CreateDepositAccountDto();
        createDepositAccountDto.setIdentificationNo("021023-08-1925");
        createDepositAccountDto.setPassword("password");
        createDepositAccountDto.setAmount(1000);

        when(customerService.verifyLogin("021023-08-1925", "password")).thenReturn(true);
        DepositAccount existingAccount = new DepositAccount();
        when(depositAccountRepository.findByCustomerIdentificationNo("021023-08-1925"))
                .thenReturn(Optional.of(existingAccount));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depositAccountManagement.createAccount(createDepositAccountDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any(DepositAccount.class));
    }

    @Test
    public void testCreateAccountWithInvalidLogin() {
        // Arrange
        CreateDepositAccountDto createDepositAccountDto = new CreateDepositAccountDto();
        createDepositAccountDto.setIdentificationNo("021023-08-1925");
        createDepositAccountDto.setPassword("wrongPassword");
        createDepositAccountDto.setAmount(1000);

        when(customerService.verifyLogin("021023-08-1925", "wrongPassword")).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountManagement.createAccount(createDepositAccountDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any(DepositAccount.class));
    }

    @Test
    public void testGetAccount() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(1000);
        account.setStatus(DepositAccount.Status.ACTIVE);
        account.setCreatedAt(new Date());

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act
        GetDepositAccountDto accountDto = depositAccountManagement.getAccount(identificationNo, password);

        // Assert
        assertNotNull(accountDto);
        assertEquals(account.getId().toString(), accountDto.getId());
        assertEquals(account.getCustomer().getId().toString(), accountDto.getCustomerId());
        assertEquals(account.getAmount(), accountDto.getAmount());
        assertEquals("ACTIVE", accountDto.getStatus());
        assertEquals(account.getCreatedAt().toString(), accountDto.getCreatedAt());
    }

    @Test
    public void testGetAccountWithInvalidLogin() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "wrongPassword";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(1000);
        account.setStatus(DepositAccount.Status.ACTIVE);
        account.setCreatedAt(new Date());

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountManagement.getAccount(identificationNo, password);
        });
        assertNotNull(exception);
    }

    @Test
    public void testCloseAccount() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(1000);
        account.setStatus(DepositAccount.Status.ACTIVE);
        account.setCreatedAt(new Date());

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act
        depositAccountManagement.closeAccount(identificationNo, password);

        // Assert
        assertEquals(DepositAccount.Status.CLOSED, account.getStatus());
        verify(depositAccountRepository, times(1)).save(account);
    }

    @Test
    public void testCloseAccountWithInvalidLogin() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "wrongPassword";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(1000);
        account.setStatus(DepositAccount.Status.ACTIVE);
        account.setCreatedAt(new Date());

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountManagement.closeAccount(identificationNo, password);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any(DepositAccount.class));
    }

    @Test
    public void testCloseAccountAlreadyClosed() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(1000);
        account.setStatus(DepositAccount.Status.CLOSED);
        account.setCreatedAt(new Date());

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depositAccountManagement.closeAccount(identificationNo, password);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any(DepositAccount.class));
    }

    // deposit fund
    @Test
    public void testDepositFundsSuccess() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(1000);
        account.setStatus(DepositAccount.Status.ACTIVE);
        account.setCreatedAt(new Date());

        DepositFundsDto depositFundsDto = new DepositFundsDto();
        depositFundsDto.setIdentificationNo(identificationNo);
        depositFundsDto.setPassword(password);
        depositFundsDto.setAmount(200);

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act
        GetDepositAccountDto result = depositAccountManagement.depositFunds(depositFundsDto);

        // Assert
        assertEquals(1200, account.getAmount());
        assertNotNull(result);
        assertEquals(1200, result.getAmount());
        assertEquals("ACTIVE", result.getStatus());
        verify(depositAccountRepository, times(1)).save(account);
    }

    @Test
    public void testDepositFundsInvalidLogin() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "wrongPassword";

        DepositFundsDto depositFundsDto = new DepositFundsDto();
        depositFundsDto.setIdentificationNo(identificationNo);
        depositFundsDto.setPassword(password);
        depositFundsDto.setAmount(200);

        when(customerService.verifyLogin(identificationNo, password)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountManagement.depositFunds(depositFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testDepositFundsNoAccountFound() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "password";

        DepositFundsDto depositFundsDto = new DepositFundsDto();
        depositFundsDto.setIdentificationNo(identificationNo);
        depositFundsDto.setPassword(password);
        depositFundsDto.setAmount(200);

        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);
        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.empty());

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountManagement.depositFunds(depositFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testDepositFundsClosedAccountNotAllowed() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(1000);
        account.setStatus(DepositAccount.Status.CLOSED);
        account.setCreatedAt(new Date());

        DepositFundsDto depositFundsDto = new DepositFundsDto();
        depositFundsDto.setIdentificationNo(identificationNo);
        depositFundsDto.setPassword(password);
        depositFundsDto.setAmount(200);

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depositAccountManagement.depositFunds(depositFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testDepositFundsFrozenAccountNotAllowed() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(1000);
        account.setStatus(DepositAccount.Status.FROZEN);
        account.setCreatedAt(new Date());

        DepositFundsDto depositFundsDto = new DepositFundsDto();
        depositFundsDto.setIdentificationNo(identificationNo);
        depositFundsDto.setPassword(password);
        depositFundsDto.setAmount(200);

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depositAccountManagement.depositFunds(depositFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testDepositFundsNonPositiveAmountRejected() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "password";

        DepositFundsDto depositFundsDto = new DepositFundsDto();
        depositFundsDto.setIdentificationNo(identificationNo);
        depositFundsDto.setPassword(password);
        depositFundsDto.setAmount(0);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountManagement.depositFunds(depositFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    // withdraw fund
    @Test
    public void testWithdrawFundsSuccess() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(1000);
        account.setStatus(DepositAccount.Status.ACTIVE);
        account.setCreatedAt(new Date());

        WithdrawFundsDto withdrawFundsDto = new WithdrawFundsDto();
        withdrawFundsDto.setIdentificationNo(identificationNo);
        withdrawFundsDto.setPassword(password);
        withdrawFundsDto.setAmount(200);

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act
        GetDepositAccountDto result = depositAccountManagement.withdrawFunds(withdrawFundsDto);

        // Assert
        assertEquals(800, account.getAmount());
        assertNotNull(result);
        assertEquals(800, result.getAmount());
        assertEquals("ACTIVE", result.getStatus());
        verify(depositAccountRepository, times(1)).save(account);
    }

    @Test
    public void testWithdrawFundsInvalidLogin() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "wrongPassword";

        WithdrawFundsDto withdrawFundsDto = new WithdrawFundsDto();
        withdrawFundsDto.setIdentificationNo(identificationNo);
        withdrawFundsDto.setPassword(password);
        withdrawFundsDto.setAmount(200);

        when(customerService.verifyLogin(identificationNo, password)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountManagement.withdrawFunds(withdrawFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testWithdrawFundsInsufficientBalance() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(100);
        account.setStatus(DepositAccount.Status.ACTIVE);
        account.setCreatedAt(new Date());

        WithdrawFundsDto withdrawFundsDto = new WithdrawFundsDto();
        withdrawFundsDto.setIdentificationNo(identificationNo);
        withdrawFundsDto.setPassword(password);
        withdrawFundsDto.setAmount(200);

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountManagement.withdrawFunds(withdrawFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testWithdrawFundsClosedAccountNotAllowed() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(1000);
        account.setStatus(DepositAccount.Status.CLOSED);
        account.setCreatedAt(new Date());

        WithdrawFundsDto withdrawFundsDto = new WithdrawFundsDto();
        withdrawFundsDto.setIdentificationNo(identificationNo);
        withdrawFundsDto.setPassword(password);
        withdrawFundsDto.setAmount(200);

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depositAccountManagement.withdrawFunds(withdrawFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testWithdrawFundsFrozenAccountNotAllowed() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(1000);
        account.setStatus(DepositAccount.Status.FROZEN);
        account.setCreatedAt(new Date());

        WithdrawFundsDto withdrawFundsDto = new WithdrawFundsDto();
        withdrawFundsDto.setIdentificationNo(identificationNo);
        withdrawFundsDto.setPassword(password);
        withdrawFundsDto.setAmount(200);

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depositAccountManagement.withdrawFunds(withdrawFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testWithdrawFundsNonPositiveAmountRejected() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "password";

        WithdrawFundsDto withdrawFundsDto = new WithdrawFundsDto();
        withdrawFundsDto.setIdentificationNo(identificationNo);
        withdrawFundsDto.setPassword(password);
        withdrawFundsDto.setAmount(-100);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountManagement.withdrawFunds(withdrawFundsDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    // freeze or unfreeze account

    @Test
    public void testFreezeAccountSuccess() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(1000);
        account.setStatus(DepositAccount.Status.ACTIVE);
        account.setCreatedAt(new Date());

        UpdateDepositStatusDto updateStatusDto = new UpdateDepositStatusDto();
        updateStatusDto.setIdentificationNo(identificationNo);
        updateStatusDto.setPassword(password);
        updateStatusDto.setAction("FREEZE");

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act
        GetDepositAccountDto result = depositAccountManagement.updateStatus(updateStatusDto);

        // Assert
        assertEquals(DepositAccount.Status.FROZEN, account.getStatus());
        assertNotNull(result);
        assertEquals("FROZEN", result.getStatus());
        verify(depositAccountRepository, times(1)).save(account);
    }

    @Test
    public void testUnfreezeAccountSuccess() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(1000);
        account.setStatus(DepositAccount.Status.FROZEN);
        account.setCreatedAt(new Date());

        UpdateDepositStatusDto updateStatusDto = new UpdateDepositStatusDto();
        updateStatusDto.setIdentificationNo(identificationNo);
        updateStatusDto.setPassword(password);
        updateStatusDto.setAction("UNFREEZE");

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act
        GetDepositAccountDto result = depositAccountManagement.updateStatus(updateStatusDto);

        // Assert
        assertEquals(DepositAccount.Status.ACTIVE, account.getStatus());
        assertNotNull(result);
        assertEquals("ACTIVE", result.getStatus());
        verify(depositAccountRepository, times(1)).save(account);
    }

    @Test
    public void testFreezeAccountAlreadyFrozen() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(1000);
        account.setStatus(DepositAccount.Status.FROZEN);
        account.setCreatedAt(new Date());

        UpdateDepositStatusDto updateStatusDto = new UpdateDepositStatusDto();
        updateStatusDto.setIdentificationNo(identificationNo);
        updateStatusDto.setPassword(password);
        updateStatusDto.setAction("FREEZE");

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depositAccountManagement.updateStatus(updateStatusDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testUnfreezeAccountWhenNotFrozen() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(1000);
        account.setStatus(DepositAccount.Status.ACTIVE);
        account.setCreatedAt(new Date());

        UpdateDepositStatusDto updateStatusDto = new UpdateDepositStatusDto();
        updateStatusDto.setIdentificationNo(identificationNo);
        updateStatusDto.setPassword(password);
        updateStatusDto.setAction("UNFREEZE");

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depositAccountManagement.updateStatus(updateStatusDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testUpdateStatusInvalidLogin() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "wrongPassword";

        UpdateDepositStatusDto updateStatusDto = new UpdateDepositStatusDto();
        updateStatusDto.setIdentificationNo(identificationNo);
        updateStatusDto.setPassword(password);
        updateStatusDto.setAction("FREEZE");

        when(customerService.verifyLogin(identificationNo, password)).thenReturn(false);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountManagement.updateStatus(updateStatusDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testUpdateStatusClosedAccountNotAllowed() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "password";

        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        DepositAccount account = new DepositAccount();
        account.setId(UUID.randomUUID());
        account.setCustomer(customer);
        account.setAmount(1000);
        account.setStatus(DepositAccount.Status.CLOSED);
        account.setCreatedAt(new Date());

        UpdateDepositStatusDto updateStatusDto = new UpdateDepositStatusDto();
        updateStatusDto.setIdentificationNo(identificationNo);
        updateStatusDto.setPassword(password);
        updateStatusDto.setAction("FREEZE");

        when(depositAccountRepository.findByCustomerIdentificationNo(identificationNo))
                .thenReturn(Optional.of(account));
        when(customerService.verifyLogin(identificationNo, password)).thenReturn(true);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            depositAccountManagement.updateStatus(updateStatusDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }

    @Test
    public void testUpdateStatusInvalidAction() {
        // Arrange
        String identificationNo = "021023-08-1925";
        String password = "password";

        UpdateDepositStatusDto updateStatusDto = new UpdateDepositStatusDto();
        updateStatusDto.setIdentificationNo(identificationNo);
        updateStatusDto.setPassword(password);
        updateStatusDto.setAction("INVALID_ACTION");

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            depositAccountManagement.updateStatus(updateStatusDto);
        });
        assertNotNull(exception);
        verify(depositAccountRepository, never()).save(any());
    }
}