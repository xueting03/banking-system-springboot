package com.wif3006.banking_system.customer.dto;

import lombok.Data;


@Data
public class UpdateCustomerDto {
    private String name;
    private String identificationNo;
    private String phoneNo;
    private String address;
    private String currentPassword; // For authenticating the user
    private String newPassword;     // New password for update
    private String status;
}
