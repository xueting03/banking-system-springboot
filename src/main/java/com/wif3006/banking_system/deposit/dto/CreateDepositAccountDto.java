package com.wif3006.banking_system.deposit.dto;

import lombok.Data;

@Data
public class CreateDepositAccountDto {

    private String identificationNo;
    private String password;
    private int amount;
}
