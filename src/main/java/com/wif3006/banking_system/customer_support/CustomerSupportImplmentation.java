package com.wif3006.banking_system.customer_support;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wif3006.banking_system.base.CustomerService;
import com.wif3006.banking_system.base.CustomerSupportService;
import com.wif3006.banking_system.base.model.Customer;
import com.wif3006.banking_system.base.model.SupportTicket;
import com.wif3006.banking_system.base.model.SupportTicket.Status;
import com.wif3006.banking_system.customer.dto.GetCustomerDto;
import com.wif3006.banking_system.customer_support.dto.AssignTicketRequestDto;
import com.wif3006.banking_system.customer_support.dto.CreateSupportTicketRequestDto;
import com.wif3006.banking_system.customer_support.dto.UpdateSupportTicketRequestDto;
import com.wif3006.banking_system.customer_support.dto.UpdateTicketStatusRequestDto;

import jakarta.transaction.Transactional;

@Service
public class CustomerSupportImplmentation implements CustomerSupportService {
    @Autowired
    private CustomerSupportRepository customerSupportRepository;

    @Autowired
    private CustomerService customerService;

    @Override
    public void openTicket(CreateSupportTicketRequestDto request) {
        String customerNumber = request.getCustomerIdNumber();
        String authSecret = request.getAuthPassword();

        boolean authenticated = customerService.verifyLogin(customerNumber, authSecret);
        if (!authenticated) {
            throw new IllegalArgumentException("Invalid customer credentials.");
        }

        GetCustomerDto customer = customerService.getProfile(customerNumber);
        Customer customerEntity = new Customer();
        customerEntity.setId(UUID.fromString(customer.getId()));
        customerEntity.setIdentificationNo(customer.getIdentificationNo());

        SupportTicket ticket = new SupportTicket();
        ticket.setCustomer(customerEntity);
        ticket.setTitle(request.getSubject());
        ticket.setDescription(request.getMessage());
        ticket.setStatus(Status.OPEN);
        ticket.setAssignedStaffId(null);

        customerSupportRepository.save(ticket);
    }

    @Override
    @Transactional
    public void reviseTicketDetails(UpdateSupportTicketRequestDto request) {
        SupportTicket ticket = customerSupportRepository.findById(UUID.fromString(request.getTicketId()))
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found."));

        boolean authenticated = customerService.verifyLogin(ticket.getCustomer().getIdentificationNo(),
                request.getAuthPassword());
        if (!authenticated) {
            throw new IllegalArgumentException("Invalid credentials for ticket owner.");
        }

        if (ticket.getStatus() == Status.RESOLVED) {
            throw new IllegalArgumentException("Resolved tickets cannot be edited.");
        }

        if (request.getSubject() != null) {
            ticket.setTitle(request.getSubject());
        }
        if (request.getMessage() != null) {
            ticket.setDescription(request.getMessage());
        }

        customerSupportRepository.save(ticket);
    }

    @Override
    @Transactional
    public void allocateTicket(AssignTicketRequestDto request) {
        SupportTicket ticket = customerSupportRepository.findById(UUID.fromString(request.getTicketId()))
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found."));

        if (ticket.getAssignedStaffId() != null && !ticket.getAssignedStaffId().isBlank()) {
            throw new IllegalArgumentException("Ticket already assigned.");
        }

        if (request.getAssigneeId() == null || request.getAssigneeId().isBlank()) {
            throw new IllegalArgumentException("Assignee id is required to allocate a ticket.");
        }

        ticket.setAssignedStaffId(request.getAssigneeId());
        customerSupportRepository.save(ticket);
    }

    @Override
    @Transactional
    public void changeTicketStatus(UpdateTicketStatusRequestDto request) {
        SupportTicket ticket = customerSupportRepository.findById(UUID.fromString(request.getTicketId()))
                .orElseThrow(() -> new IllegalArgumentException("Ticket not found."));

        if (request.getStatus() == null) {
            throw new IllegalArgumentException("Status is required.");
        }

        String assignee = ticket.getAssignedStaffId();
        if (assignee == null || assignee.isBlank()) {
            throw new IllegalArgumentException("Ticket is not assigned to any staff.");
        }
        if (request.getActionedBy() == null || !assignee.equals(request.getActionedBy())) {
            throw new IllegalArgumentException("Only the assigned staff member may update status.");
        }

        ticket.setStatus(request.getStatus());
        customerSupportRepository.save(ticket);
    }
}
