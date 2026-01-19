package com.wif3006.banking_system.card.dto;

import lombok.Data;

@Data
public class UpdateCardPinDto {
    private String identificationNo;
    private String password;
    private String currentPin;
    private String newPin;
}
