package com.wif3006.banking_system.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.wif3006.banking_system.model.DepositAccount;

public interface DepositAccountRepository extends JpaRepository<DepositAccount, UUID> {
    Optional<DepositAccount> findByCustomerId(UUID customerId);
    Optional<DepositAccount> findByCustomerIdentificationNo(String identificationNo);
}
