package com.wif3006.banking_system.deposit.dto;

import lombok.Data;

@Data
public class GetDepositAccountDto {
    private String id;
    private String customerId;
    private int amount;
    private String status;
    private String createdAt; 
}
