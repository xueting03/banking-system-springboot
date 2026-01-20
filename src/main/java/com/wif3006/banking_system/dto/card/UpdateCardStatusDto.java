package com.wif3006.banking_system.dto.card;

import lombok.Data;

@Data
public class UpdateCardStatusDto {
    private String identificationNo;
    private String password;
    private Action action;
    private String pinNumber;

    public enum Action { ACTIVATE, DEACTIVATE, FREEZE, UNFREEZE }
}
