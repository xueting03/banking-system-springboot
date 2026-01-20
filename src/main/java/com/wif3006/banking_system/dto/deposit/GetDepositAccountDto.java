package com.wif3006.banking_system.dto.deposit;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class GetDepositAccountDto {
    private String id;
    private String customerId;
    private BigDecimal amount;
    private String status;
    private LocalDateTime createdAt; 
}
