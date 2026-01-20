package com.wif3006.banking_system.dto.support;

import lombok.Data;

@Data
public class CreateSupportTicketRequestDto {
    private String customerIdNumber;
    private String authPassword;
    private String subject;
    private String message;
}
