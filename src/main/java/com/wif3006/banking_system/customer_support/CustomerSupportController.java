package com.wif3006.banking_system.customer_support;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wif3006.banking_system.base.CustomerSupportService;
import com.wif3006.banking_system.customer_support.dto.AssignTicketRequestDto;
import com.wif3006.banking_system.customer_support.dto.CreateSupportTicketRequestDto;
import com.wif3006.banking_system.customer_support.dto.UpdateSupportTicketRequestDto;
import com.wif3006.banking_system.customer_support.dto.UpdateTicketStatusRequestDto;

@RestController
@RequestMapping("/api/customer-support")
public class CustomerSupportController {

    @Autowired
    private CustomerSupportService customerSupportService;

    @PostMapping("/tickets")
    public ResponseEntity<String> createTicket(@RequestBody CreateSupportTicketRequestDto request) {
        try {
            customerSupportService.openTicket(request);
            return ResponseEntity.ok("Ticket created.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }

    @PatchMapping("/tickets/details")
    public ResponseEntity<String> updateTicketDetails(@RequestBody UpdateSupportTicketRequestDto request) {
        try {
            customerSupportService.reviseTicketDetails(request);
            return ResponseEntity.ok("Ticket details updated.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }

    @PatchMapping("/tickets/assignee")
    public ResponseEntity<String> assignTicket(@RequestBody AssignTicketRequestDto request) {
        try {
            customerSupportService.allocateTicket(request);
            return ResponseEntity.ok("Ticket assigned.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }

    @PatchMapping("/tickets/status")
    public ResponseEntity<String> updateTicketStatus(@RequestBody UpdateTicketStatusRequestDto request) {
        try {
            customerSupportService.changeTicketStatus(request);
            return ResponseEntity.ok("Ticket status updated.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Unexpected error: " + e.getMessage());
        }
    }
}
