package com.wif3006.banking_system.dto.deposit;

import lombok.Data;

@Data
public class UpdateDepositStatusDto {
    private String identificationNo;
    private String password;
    private String action; // FREEZE or UNFREEZE
}
