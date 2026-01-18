package com.wif3006.banking_system.base;

import java.security.NoSuchAlgorithmException;

import com.wif3006.banking_system.customer.dto.CreateCustomerDto;
import com.wif3006.banking_system.customer.dto.GetCustomerDto;
import com.wif3006.banking_system.customer.dto.UpdateCustomerDto;

public interface CustomerService {
    void createProfile(CreateCustomerDto customerDto) throws NoSuchAlgorithmException;
    GetCustomerDto getProfile(String identificationNo);
    boolean updateProfile(String identificationNo, UpdateCustomerDto updatedCustomerDto) throws NoSuchAlgorithmException;
    boolean verifyLogin(String identificationNo, String password) ;
}
