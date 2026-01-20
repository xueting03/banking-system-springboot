package com.wif3006.banking_system.dto.customer;

import lombok.Data;

@Data
public class CreateCustomerDto {
    private String name;

    private String identificationNo;

    private String phoneNo;

    private String address;

    private String password;
}
