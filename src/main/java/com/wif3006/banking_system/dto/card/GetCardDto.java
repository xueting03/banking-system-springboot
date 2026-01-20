package com.wif3006.banking_system.dto.card;

import lombok.Data;

@Data
public class GetCardDto {
    private String identificationNo;
    private String password;
}
