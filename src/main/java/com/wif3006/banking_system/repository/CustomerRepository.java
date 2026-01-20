package com.wif3006.banking_system.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wif3006.banking_system.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    /**
     * Retrieve a customer entity using the provided identification number.
     * Useful for searching customers by their government-issued ID.
     */
    Optional<Customer> findByIdentificationNo(String identificationNo);

    /**
     * Lookup a customer record based on their phone number.
     * Handy for login validation or contact-based queries.
     */
    Optional<Customer> findByPhoneNo(String phoneNo);
}
