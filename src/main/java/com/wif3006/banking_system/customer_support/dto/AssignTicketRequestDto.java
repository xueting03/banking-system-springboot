package com.wif3006.banking_system.customer_support.dto;

import lombok.Data;

@Data
public class AssignTicketRequestDto {
    private String ticketId;
    private String assigneeId;
}
