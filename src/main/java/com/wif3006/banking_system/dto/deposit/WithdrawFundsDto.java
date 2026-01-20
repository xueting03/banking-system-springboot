package com.wif3006.banking_system.dto.deposit;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class WithdrawFundsDto {
    private String identificationNo;
    private String password;
    private BigDecimal amount;
}
