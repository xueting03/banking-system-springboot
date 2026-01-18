package com.wif3006.banking_system.customer;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wif3006.banking_system.base.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    /**
     * Find a customer by their identification number.
     * This can be useful for additional lookup functionality.
     */
    Optional<Customer> findByIdentificationNo(String identificationNo);

    /**
     * Find a customer by their phone number.
     * This can be useful for authentication or other checks.
     */
    Optional<Customer> findByPhoneNo(String phoneNo);
}
