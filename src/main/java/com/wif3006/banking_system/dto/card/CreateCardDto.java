package com.wif3006.banking_system.dto.card;

import lombok.Data;

@Data
public class CreateCardDto {
    private String identificationNo;
    private String password;
    private String pinNumber;
}
