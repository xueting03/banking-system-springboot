package com.wif3006.banking_system.dto.customer;

import lombok.Data;


@Data
public class UpdateCustomerDto {
    // Fields for updating customer profile details
    private String name;
    private String identificationNo;
    private String phoneNo;
    private String address;
    private String currentPassword; // Used to verify user identity
    private String newPassword;     // If user wants to change password
}
