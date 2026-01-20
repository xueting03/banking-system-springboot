package com.wif3006.banking_system.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.wif3006.banking_system.model.SupportTicket;

@Repository
public interface CustomerSupportRepository extends JpaRepository<SupportTicket, UUID> {
    List<SupportTicket> findAllByCustomerId(UUID customerId);
}
