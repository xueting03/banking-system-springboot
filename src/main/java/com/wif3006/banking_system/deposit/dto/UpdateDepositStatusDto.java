package com.wif3006.banking_system.deposit.dto;

import lombok.Data;

@Data
public class UpdateDepositStatusDto {
    private String identificationNo;
    private String password;
    private String action; // FREEZE or UNFREEZE
}
