package com.wif3006.banking_system.card.dto;

import lombok.Data;

@Data
public class UpdateCardLimitDto {
    private String identificationNo;
    private String password;
    private int newLimit;
    private String pinNumber;
}
