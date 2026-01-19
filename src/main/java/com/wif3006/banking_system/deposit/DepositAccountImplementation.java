package com.wif3006.banking_system.deposit;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.wif3006.banking_system.base.CustomerService;
import com.wif3006.banking_system.base.DepositAccountService;
import com.wif3006.banking_system.base.model.Customer;
import com.wif3006.banking_system.base.model.DepositAccount;
import com.wif3006.banking_system.customer.dto.GetCustomerDto;
import com.wif3006.banking_system.deposit.dto.CreateDepositAccountDto;
import com.wif3006.banking_system.deposit.dto.DepositFundsDto;
import com.wif3006.banking_system.deposit.dto.GetDepositAccountDto;
import com.wif3006.banking_system.deposit.dto.UpdateDepositStatusDto;
import com.wif3006.banking_system.deposit.dto.WithdrawFundsDto;

@Service
public class DepositAccountImplementation implements DepositAccountService {
    @Autowired
    private DepositAccountRepository depositAccountRepository;

    @Autowired
    private CustomerService customerService;

    /**
     * Create a new deposit account for the authenticated customer.
     */
    @Override
    public void createAccount(CreateDepositAccountDto createDepositAccountDto) {
        String customerIdNumber = createDepositAccountDto.getIdentificationNo();
        String userPassword = createDepositAccountDto.getPassword();
        BigDecimal initialAmount = createDepositAccountDto.getAmount();

        // Verify customer credentials before proceeding
        boolean isAuthenticated = customerService.verifyLogin(customerIdNumber, userPassword);
        if (!isAuthenticated) {
            throw new IllegalArgumentException("Invalid credentials provided.");
        }

        // Ensure customer doesn't have an existing deposit account
        Optional<DepositAccount> currentAccount = depositAccountRepository.findByCustomerIdentificationNo(customerIdNumber);
        if (currentAccount.isPresent()) {
            throw new IllegalStateException("An active deposit account already exists for this customer.");
        }

        // Initialize new deposit account entity
        GetCustomerDto customerProfile = customerService.getProfile(customerIdNumber);
        Customer accountOwner = new Customer();
        accountOwner.setId(UUID.fromString(customerProfile.getId()));
        
        DepositAccount newAccount = new DepositAccount();
        newAccount.setCustomer(accountOwner);
        newAccount.setAmount(initialAmount);
        newAccount.setStatus(DepositAccount.Status.ACTIVE);
        newAccount.setCreatedAt(LocalDateTime.now());

        depositAccountRepository.save(newAccount);
    }
    
    @Override
    public GetDepositAccountDto getAccount(String identificationNo, String password) {
        // Retrieve deposit account by identification number
        DepositAccount retrievedAccount = depositAccountRepository.findByCustomerIdentificationNo(identificationNo)
                .orElseThrow(() -> new IllegalArgumentException("No deposit account found for this customer."));
        
        // Authenticate user credentials
        boolean validCredentials = customerService.verifyLogin(identificationNo, password);
        if (!validCredentials) {
            throw new IllegalArgumentException("Authentication failed. Invalid credentials.");
        }
        
        // Build response DTO with account details
        GetDepositAccountDto responseDto = new GetDepositAccountDto();
        responseDto.setId(retrievedAccount.getId().toString());
        responseDto.setCustomerId(retrievedAccount.getCustomer().getId().toString());
        responseDto.setAmount(retrievedAccount.getAmount());
        responseDto.setStatus(retrievedAccount.getStatus().toString());
        responseDto.setCreatedAt(retrievedAccount.getCreatedAt());
        return responseDto;
    }

    @Override
    public void closeAccount(String identificationNo, String password) {
        // Locate the deposit account to be closed
        DepositAccount targetAccount = depositAccountRepository.findByCustomerIdentificationNo(identificationNo)
                .orElseThrow(() -> new IllegalArgumentException("Unable to locate account with provided credentials."));
        
        // Verify account is not already in closed state
        if (targetAccount.getStatus() == DepositAccount.Status.CLOSED) {
            throw new IllegalStateException("This account has already been closed.");
        }
        
        // Authenticate customer before allowing closure
        boolean isAuthorized = customerService.verifyLogin(identificationNo, password);
        if (!isAuthorized) {
            throw new IllegalArgumentException("Authorization failed. Invalid credentials provided.");
        }
        
        // Update account status to closed and persist
        targetAccount.setStatus(DepositAccount.Status.CLOSED);
        depositAccountRepository.save(targetAccount);
    }

    /**
     * Processes fund deposit transactions for active accounts.
     */
    @Override
    @Transactional
    public GetDepositAccountDto depositFunds(DepositFundsDto depositFundsDto) {
        String customerIdentifier = depositFundsDto.getIdentificationNo();
        String accessPassword = depositFundsDto.getPassword();
        BigDecimal depositAmount = depositFundsDto.getAmount();

        // Ensure deposit amount is positive
        if (depositAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Deposit value must exceed zero.");
        }

        boolean authenticationSuccess = customerService.verifyLogin(customerIdentifier, accessPassword);
        if (!authenticationSuccess) {
            throw new IllegalArgumentException("Authentication failure: Invalid account credentials.");
        }
        DepositAccount depositAccount = depositAccountRepository.findByCustomerIdentificationNo(customerIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("Deposit account not located."));

        // Check if account permits deposits
        if (depositAccount.getStatus() == DepositAccount.Status.CLOSED) {
            throw new IllegalStateException("Deposits not permitted for closed accounts.");
        }
        if (depositAccount.getStatus() == DepositAccount.Status.FROZEN) {
            throw new IllegalStateException("Deposits blocked on frozen accounts.");
        }

        depositAccount.setAmount(depositAccount.getAmount().add(depositAmount));
        depositAccountRepository.save(depositAccount);
        return convertToDto(depositAccount);
    }

    /**
     * Handles withdrawal transactions from deposit accounts.
     */
    @Override
    @Transactional
    public GetDepositAccountDto withdrawFunds(WithdrawFundsDto withdrawFundsDto) {
        String accountIdentifier = withdrawFundsDto.getIdentificationNo();
        String userPassword = withdrawFundsDto.getPassword();
        BigDecimal withdrawalAmount = withdrawFundsDto.getAmount();

        // Verify withdrawal amount is valid
        if (withdrawalAmount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Withdrawal value must be positive.");
        }

        boolean credentialsValid = customerService.verifyLogin(accountIdentifier, userPassword);
        if (!credentialsValid) {
            throw new IllegalArgumentException("Credential verification failed.");
        }
        DepositAccount accountRecord = depositAccountRepository.findByCustomerIdentificationNo(accountIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("Account record not found."));

        // Verify account allows withdrawals
        if (accountRecord.getStatus() == DepositAccount.Status.CLOSED) {
            throw new IllegalStateException("Withdrawals prohibited from closed accounts.");
        }
        if (accountRecord.getStatus() == DepositAccount.Status.FROZEN) {
            throw new IllegalStateException("Withdrawals suspended for frozen accounts.");
        }

        // Check available balance
        if (accountRecord.getAmount().compareTo(withdrawalAmount) < 0) {
            throw new IllegalArgumentException("Available balance insufficient.");
        }

        // Process withdrawal
        accountRecord.setAmount(accountRecord.getAmount().subtract(withdrawalAmount));
        depositAccountRepository.save(accountRecord);
        return convertToDto(accountRecord);
    }

    /**
     * Modifies account operational status (FREEZE or UNFREEZE).
     */
    @Override
    @Transactional
    public GetDepositAccountDto updateStatus(UpdateDepositStatusDto updateDepositStatusDto) {
        String customerIdentifier = updateDepositStatusDto.getIdentificationNo();
        String authPassword = updateDepositStatusDto.getPassword();
        String requestedAction = updateDepositStatusDto.getAction();

        if (requestedAction == null || (!requestedAction.equalsIgnoreCase("FREEZE") && !requestedAction.equalsIgnoreCase("UNFREEZE"))) {
            throw new IllegalArgumentException("Action must be either FREEZE or UNFREEZE.");
        }

        boolean authenticationPassed = customerService.verifyLogin(customerIdentifier, authPassword);
        if (!authenticationPassed) {
            throw new IllegalArgumentException("Credential authentication failed.");
        }
        DepositAccount accountEntity = depositAccountRepository.findByCustomerIdentificationNo(customerIdentifier)
                .orElseThrow(() -> new IllegalArgumentException("No matching deposit account found."));

        // Closed accounts cannot be modified
        if (accountEntity.getStatus() == DepositAccount.Status.CLOSED) {
            throw new IllegalStateException("Status modification not allowed for closed accounts.");
        }

        // Execute requested action
        if (requestedAction.equalsIgnoreCase("FREEZE")) {
            if (accountEntity.getStatus() == DepositAccount.Status.FROZEN) {
                throw new IllegalStateException("Account is currently frozen.");
            }
            accountEntity.setStatus(DepositAccount.Status.FROZEN);
        } else if (requestedAction.equalsIgnoreCase("UNFREEZE")) {
            if (accountEntity.getStatus() == DepositAccount.Status.ACTIVE) {
                throw new IllegalStateException("Account is already active.");
            }
            accountEntity.setStatus(DepositAccount.Status.ACTIVE);
        }

        depositAccountRepository.save(accountEntity);
        return convertToDto(accountEntity);
    }

    /**
     * Helper method to convert DepositAccount entity to DTO.
     */
    private GetDepositAccountDto convertToDto(DepositAccount account) {
        GetDepositAccountDto accountDto = new GetDepositAccountDto();
        accountDto.setId(account.getId().toString());
        accountDto.setCustomerId(account.getCustomer().getId().toString());
        accountDto.setAmount(account.getAmount());
        accountDto.setStatus(account.getStatus().toString());
        accountDto.setCreatedAt(account.getCreatedAt());
        return accountDto;
    }
}