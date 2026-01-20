package com.wif3006.banking_system.dto.support;

import lombok.Data;

@Data
public class AssignTicketRequestDto {
    private String ticketId;
    private String assigneeId;
}
