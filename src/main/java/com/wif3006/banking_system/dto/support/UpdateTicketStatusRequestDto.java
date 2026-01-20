package com.wif3006.banking_system.dto.support;

import com.wif3006.banking_system.model.SupportTicket.Status;

import lombok.Data;

@Data
public class UpdateTicketStatusRequestDto {
    private String ticketId;
    private Status status;
    private String actionedBy;
}
