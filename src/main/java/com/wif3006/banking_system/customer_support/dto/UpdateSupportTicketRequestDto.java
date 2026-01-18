package com.wif3006.banking_system.customer_support.dto;

import lombok.Data;

@Data
public class UpdateSupportTicketRequestDto {
    private String ticketId;
    private String authPassword;
    private String subject;
    private String message;
}
