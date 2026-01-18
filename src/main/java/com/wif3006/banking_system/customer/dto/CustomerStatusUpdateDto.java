package com.wif3006.banking_system.customer.dto;

import lombok.Data;

/**
 * DTO for updating only the status (e.g., activate/deactivate) of a customer account.
 */
@Data
public class CustomerStatusUpdateDto {
    private String identificationNo;
    private String status; 
}