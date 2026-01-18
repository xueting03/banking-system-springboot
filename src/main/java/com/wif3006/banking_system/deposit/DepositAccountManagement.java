package com.wif3006.banking_system.deposit;

import java.util.Date;
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
public class DepositAccountManagement implements DepositAccountService {
    @Autowired
    private DepositAccountRepository depositAccountRepository;

    @Autowired
    private CustomerService customerService;

    /**
     * Creates a deposit account for a customer if the password is correct.
     */
    @Override
    public void createAccount(CreateDepositAccountDto createDepositAccountDto) {
        String identificationNo = createDepositAccountDto.getIdentificationNo();
        String password = createDepositAccountDto.getPassword();
        int amount = createDepositAccountDto.getAmount();

        boolean customerLoggedOn = customerService.verifyLogin(identificationNo, password);
        if (!customerLoggedOn) {
            throw new IllegalArgumentException("Wrong customer ID or password.");
        }

        // Check if the customer already has a deposit account
        Optional<DepositAccount> existingAccount = depositAccountRepository.findByCustomerIdentificationNo(identificationNo);
        if (existingAccount.isPresent()) {
            throw new IllegalStateException("Customer already has a deposit account.");
        }

        // Create and save the deposit account
        DepositAccount depositAccount = new DepositAccount();
        GetCustomerDto customerDto = customerService.getProfile(identificationNo);
        Customer customer = new Customer();
        customer.setId(UUID.fromString(customerDto.getId()));
        depositAccount.setCustomer(customer);
        depositAccount.setAmount(amount);
        depositAccount.setStatus(DepositAccount.Status.ACTIVE);
        depositAccount.setCreatedAt(new Date());

        depositAccountRepository.save(depositAccount);
    }
    
    @Override
    public GetDepositAccountDto getAccount(String identificationNo, String password) {
        DepositAccount account = depositAccountRepository.findByCustomerIdentificationNo(identificationNo).orElseThrow(() -> new IllegalArgumentException("Customer does not have a deposit account."));
        boolean customerLoggedOn = customerService.verifyLogin(identificationNo,password);
        if(!customerLoggedOn) {
            throw new IllegalArgumentException("Wrong account ID or password.");
        }
        GetDepositAccountDto accountDto = new GetDepositAccountDto();
        accountDto.setId(account.getId().toString());
        accountDto.setCustomerId(account.getCustomer().getId().toString());
        accountDto.setAmount(account.getAmount());
        accountDto.setStatus(account.getStatus().toString());
        accountDto.setCreatedAt(account.getCreatedAt().toString());
        return accountDto;
    }

    @Override
    public void closeAccount(String identificationNo, String password) {
        DepositAccount accountToClose = depositAccountRepository.findByCustomerIdentificationNo(identificationNo).orElseThrow(() -> new IllegalArgumentException("Wrong account ID or password."));
        if(accountToClose.getStatus() == DepositAccount.Status.CLOSED) {
            throw new IllegalStateException("Account is already closed.");
        }
        boolean customerLoggedOn = customerService.verifyLogin(identificationNo, password);
        if (!customerLoggedOn) {
            throw new IllegalArgumentException("Wrong account ID or password.");
        }
        accountToClose.setStatus(DepositAccount.Status.CLOSED);
        depositAccountRepository.save(accountToClose);
    }

    /**
     * Deposits funds into a deposit account.
     */
    @Override
    @Transactional
    public GetDepositAccountDto depositFunds(DepositFundsDto depositFundsDto) {
        String identificationNo = depositFundsDto.getIdentificationNo();
        String password = depositFundsDto.getPassword();
        int amount = depositFundsDto.getAmount();

        // Validate amount
        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero.");
        }

        boolean customerLoggedOn = customerService.verifyLogin(identificationNo, password);
        if (!customerLoggedOn) {
            throw new IllegalArgumentException("Wrong account ID or password.");
        }
        DepositAccount account = depositAccountRepository.findByCustomerIdentificationNo(identificationNo)
                .orElseThrow(() -> new IllegalArgumentException("No deposit account is found."));

        // Validate account status
        if (account.getStatus() == DepositAccount.Status.CLOSED) {
            throw new IllegalStateException("Cannot deposit to a closed account.");
        }
        if (account.getStatus() == DepositAccount.Status.FROZEN) {
            throw new IllegalStateException("Cannot deposit to a frozen account.");
        }

        account.setAmount(account.getAmount() + amount);
        depositAccountRepository.save(account);
        return convertToDto(account);
    }

    /**
     * Withdraws funds from a deposit account.
     */
    @Override
    @Transactional
    public GetDepositAccountDto withdrawFunds(WithdrawFundsDto withdrawFundsDto) {
        String identificationNo = withdrawFundsDto.getIdentificationNo();
        String password = withdrawFundsDto.getPassword();
        int amount = withdrawFundsDto.getAmount();

        // Validate amount
        if (amount <= 0) {
            throw new IllegalArgumentException("Withdraw amount must be greater than zero.");
        }

        boolean customerLoggedOn = customerService.verifyLogin(identificationNo, password);
        if (!customerLoggedOn) {
            throw new IllegalArgumentException("Wrong account ID or password.");
        }
        DepositAccount account = depositAccountRepository.findByCustomerIdentificationNo(identificationNo)
                .orElseThrow(() -> new IllegalArgumentException("No deposit account is found."));

        // Validate account status
        if (account.getStatus() == DepositAccount.Status.CLOSED) {
            throw new IllegalStateException("Cannot withdraw from a closed account.");
        }
        if (account.getStatus() == DepositAccount.Status.FROZEN) {
            throw new IllegalStateException("Cannot withdraw from a frozen account.");
        }

        // Validate sufficient balance
        if (account.getAmount() < amount) {
            throw new IllegalArgumentException("Insufficient balance.");
        }

        // Debit amount from balance
        account.setAmount(account.getAmount() - amount);
        depositAccountRepository.save(account);
        return convertToDto(account);
    }

    /**
     * Updates deposit account status (FREEZE or UNFREEZE).
     */
    @Override
    @Transactional
    public GetDepositAccountDto updateStatus(UpdateDepositStatusDto updateDepositStatusDto) {
        String identificationNo = updateDepositStatusDto.getIdentificationNo();
        String password = updateDepositStatusDto.getPassword();
        String action = updateDepositStatusDto.getAction();

        if (action == null || (!action.equalsIgnoreCase("FREEZE") && !action.equalsIgnoreCase("UNFREEZE"))) {
            throw new IllegalArgumentException("Invalid action. Must be FREEZE or UNFREEZE.");
        }

        boolean customerLoggedOn = customerService.verifyLogin(identificationNo, password);
        if (!customerLoggedOn) {
            throw new IllegalArgumentException("Wrong account ID or password.");
        }
        DepositAccount account = depositAccountRepository.findByCustomerIdentificationNo(identificationNo)
                .orElseThrow(() -> new IllegalArgumentException("No deposit account is found."));

        // Cannot modify closed accounts
        if (account.getStatus() == DepositAccount.Status.CLOSED) {
            throw new IllegalStateException("Cannot modify status of a closed account.");
        }

        // Process action
        if (action.equalsIgnoreCase("FREEZE")) {
            if (account.getStatus() == DepositAccount.Status.FROZEN) {
                throw new IllegalStateException("Account is already frozen.");
            }
            account.setStatus(DepositAccount.Status.FROZEN);
        } else if (action.equalsIgnoreCase("UNFREEZE")) {
            if (account.getStatus() == DepositAccount.Status.ACTIVE) {
                throw new IllegalStateException("Account is not frozen.");
            }
            account.setStatus(DepositAccount.Status.ACTIVE);
        }

        depositAccountRepository.save(account);
        return convertToDto(account);
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
        accountDto.setCreatedAt(account.getCreatedAt().toString());
        return accountDto;
    }
}