package com.wif3006.banking_system.customer.dto;

import java.util.UUID;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GetCustomerDto {
    private String id;
    private String name;
    private String identificationNo;
    private String phoneNo;
    private String address;

    public void setId(UUID id) {
        this.id = id.toString();
    }
}