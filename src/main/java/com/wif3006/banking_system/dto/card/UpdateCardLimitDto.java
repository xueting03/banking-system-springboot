package com.wif3006.banking_system.dto.card;

import lombok.Data;

@Data
public class UpdateCardLimitDto {
    private String identificationNo;
    private String password;
    private int newLimit;
    private String pinNumber;
}
