package com.wif3006.banking_system.service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wif3006.banking_system.model.Customer;
import com.wif3006.banking_system.repository.CustomerRepository;
import com.wif3006.banking_system.dto.customer.CreateCustomerDto;
import com.wif3006.banking_system.dto.customer.GetCustomerDto;
import com.wif3006.banking_system.dto.customer.UpdateCustomerDto;
import com.wif3006.banking_system.dto.customer.CustomerStatusUpdateDto;


@Service
public class CustomerImplementation implements CustomerService {
    // Checks if password meets security requirements
    private boolean passwordMeetsPolicy(String pwd) {
        if (pwd == null) return false;
        return pwd.length() >= 8 && pwd.matches(".*\\d.*") && pwd.matches(".*[a-zA-Z].*");
    }

    @Autowired
    private CustomerRepository repository;

    // Register a new customer
    public void createProfile(CreateCustomerDto dto) throws NoSuchAlgorithmException {
        if (!passwordMeetsPolicy(dto.getPassword())) {
            throw new IllegalArgumentException("Password must be at least 8 characters, include a digit and a letter.");
        }
        Customer entity = new Customer();
        entity.setName(dto.getName());
        entity.setIdentificationNo(dto.getIdentificationNo());
        entity.setPhoneNo(dto.getPhoneNo());
        entity.setAddress(dto.getAddress());
        entity.setPassword(hashPassword(dto.getPassword()));
        entity.setStatus("ACTIVE");
        repository.save(entity);
    }

    // Lookup customer by identification number
    public GetCustomerDto getProfile(String idNo) {
        Optional<Customer> found = repository.findByIdentificationNo(idNo);
        if (found.isPresent()) {
            Customer c = found.get();
            GetCustomerDto dto = new GetCustomerDto();
            dto.setId(c.getId());
            dto.setName(c.getName());
            dto.setIdentificationNo(c.getIdentificationNo());
            dto.setPhoneNo(c.getPhoneNo());
            dto.setAddress(c.getAddress());
            return dto;
        }
        return null;
    }

    // Edit customer profile
    public boolean updateProfile(String idNo, UpdateCustomerDto updateDto) throws NoSuchAlgorithmException {
        Optional<Customer> found = repository.findByIdentificationNo(idNo);
        if (found.isPresent()) {
            Customer entity = found.get();
            if (updateDto.getCurrentPassword() == null || 
                !verifyPassword(updateDto.getCurrentPassword(), entity.getPassword())) {
                return false;
            }
            if (updateDto.getName() != null) {
                entity.setName(updateDto.getName());
            }
            if (updateDto.getIdentificationNo() != null) {
                entity.setIdentificationNo(updateDto.getIdentificationNo());
            }
            if (updateDto.getPhoneNo() != null) {
                entity.setPhoneNo(updateDto.getPhoneNo());
            }
            if (updateDto.getAddress() != null) {
                entity.setAddress(updateDto.getAddress());
            }
            if (updateDto.getNewPassword() != null && !updateDto.getNewPassword().isEmpty()) {
                if (!passwordMeetsPolicy(updateDto.getNewPassword())) {
                    throw new IllegalArgumentException("Password must be at least 8 characters, include a digit and a letter.");
                }
                entity.setPassword(hashPassword(updateDto.getNewPassword()));
            }
            repository.save(entity);
            return true;
        }
        return false;
    }

    // For updating customer status (activate/deactivate)
    public boolean updateStatus(CustomerStatusUpdateDto statusDto) {
        Optional<Customer> found = repository.findByIdentificationNo(statusDto.getIdentificationNo());
        if (found.isPresent()) {
            Customer entity = found.get();
            entity.setStatus(statusDto.getStatus());
            repository.save(entity);
            return true;
        }
        return false;
    }

    // Authenticate customer login
    public boolean verifyLogin(String idNo, String pwd) {
        Optional<Customer> found = repository.findByIdentificationNo(idNo);
        if (found.isPresent()) {
            Customer c = found.get();
            if (!"ACTIVE".equalsIgnoreCase(c.getStatus())) {
                return false;
            }
            try {
                return verifyPassword(pwd, c.getPassword());
            } catch(Exception e) {
                return false;
            }
        }
        return false;
    }

    // Hash password using SHA-256
    public String hashPassword(String pwd) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashed = md.digest(pwd.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashed) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    // Compare raw and hashed password
    private boolean verifyPassword(String raw, String hashed) throws NoSuchAlgorithmException {
        return hashPassword(raw).equals(hashed);
    }
}
