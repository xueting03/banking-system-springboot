package com.wif3006.banking_system.customer_support.dto;

import com.wif3006.banking_system.base.model.SupportTicket.Status;

import lombok.Data;

@Data
public class UpdateTicketStatusRequestDto {
    private String ticketId;
    private Status status;
    private String actionedBy;
}
