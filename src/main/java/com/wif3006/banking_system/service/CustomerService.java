package com.wif3006.banking_system.service;

import java.security.NoSuchAlgorithmException;

import com.wif3006.banking_system.dto.customer.CreateCustomerDto;
import com.wif3006.banking_system.dto.customer.CustomerStatusUpdateDto;
import com.wif3006.banking_system.dto.customer.GetCustomerDto;
import com.wif3006.banking_system.dto.customer.UpdateCustomerDto;

public interface CustomerService {
    void createProfile(CreateCustomerDto customerDto) throws NoSuchAlgorithmException;
    GetCustomerDto getProfile(String identificationNo);
    boolean updateProfile(String identificationNo, UpdateCustomerDto updatedCustomerDto) throws NoSuchAlgorithmException;
    boolean updateStatus(CustomerStatusUpdateDto statusDto);
    boolean verifyLogin(String identificationNo, String password);
}
