package com.wif3006.banking_system.customer;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.wif3006.banking_system.base.CustomerService;
import com.wif3006.banking_system.base.model.Customer;
import com.wif3006.banking_system.customer.dto.CreateCustomerDto;
import com.wif3006.banking_system.customer.dto.GetCustomerDto;
import com.wif3006.banking_system.customer.dto.UpdateCustomerDto;

@Service
public class CustomerManagement implements CustomerService {
    /**
     * Validates the password against required criteria.
     * Criteria: at least 8 chars, contains digit, contains letter
     */
    private boolean isPasswordValid(String password) {
        if (password == null) return false;
        return password.length() >= 8 &&
               password.matches(".*\\d.*") &&
               password.matches(".*[a-zA-Z].*");
    }

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * Create a new customer profile.
     */
    public void createProfile(CreateCustomerDto customerDto) throws NoSuchAlgorithmException {
        // Validate password criteria
        if (!isPasswordValid(customerDto.getPassword())) {
            throw new IllegalArgumentException("Password does not meet criteria: must be at least 8 characters, contain a digit and a letter.");
        }
        // Convert DTO to Entity
        Customer customer = new Customer();
        customer.setName(customerDto.getName());
        customer.setIdentificationNo(customerDto.getIdentificationNo());
        customer.setPhoneNo(customerDto.getPhoneNo());
        customer.setAddress(customerDto.getAddress());
        customer.setPassword(hashPassword(customerDto.getPassword())); // Hash the password
        customer.setStatus("ACTIVE");
        // Save the customer
        customerRepository.save(customer);
    }

    /**
     * Retrieve customer profile by ID.
     */
    public GetCustomerDto getProfile(String identificationNo) {
        Optional<Customer> customerOptional = customerRepository.findByIdentificationNo(identificationNo);
        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();
            GetCustomerDto customerDto = new GetCustomerDto();
            customerDto.setId(customer.getId());
            customerDto.setName(customer.getName());
            customerDto.setIdentificationNo(customer.getIdentificationNo());
            customerDto.setPhoneNo(customer.getPhoneNo());
            customerDto.setAddress(customer.getAddress());
            return customerDto;
        }
        return null;
    }

    /**
     * Update an existing customer profile.
     */
    public boolean updateProfile(String identificationNo, UpdateCustomerDto updatedCustomerDto) throws NoSuchAlgorithmException {
        Optional<Customer> customerOptional = customerRepository.findByIdentificationNo(identificationNo);
        if (customerOptional.isPresent()) {
            Customer existingCustomer = customerOptional.get();

            // Authenticate the customer by checking current password
            if (updatedCustomerDto.getCurrentPassword() == null ||
                !verifyPassword(updatedCustomerDto.getCurrentPassword(), existingCustomer.getPassword())) {
                return false; // Incorrect password
            }

            // Update fields if provided (name, identificationNo, phoneNo, address,status)
            if (updatedCustomerDto.getName() != null) {
                existingCustomer.setName(updatedCustomerDto.getName());
            }
            if (updatedCustomerDto.getIdentificationNo() != null) {
                existingCustomer.setIdentificationNo(updatedCustomerDto.getIdentificationNo());
            }
            if (updatedCustomerDto.getPhoneNo() != null) {
                existingCustomer.setPhoneNo(updatedCustomerDto.getPhoneNo());
            }
            if (updatedCustomerDto.getAddress() != null) {
                existingCustomer.setAddress(updatedCustomerDto.getAddress());
            }
            if (updatedCustomerDto.getStatus() != null) {
                existingCustomer.setStatus(updatedCustomerDto.getStatus());
            }


            // Update password if newPassword is provided and valid
            if (updatedCustomerDto.getNewPassword() != null && !updatedCustomerDto.getNewPassword().isEmpty()) {
                if (!isPasswordValid(updatedCustomerDto.getNewPassword())) {
                    throw new IllegalArgumentException("Password does not meet criteria: must be at least 8 characters, contain a digit and a letter.");
                }
                existingCustomer.setPassword(hashPassword(updatedCustomerDto.getNewPassword())); // Hash the new password
            }

            // Save updated customer profile
            customerRepository.save(existingCustomer);
            return true; // Successfully updated
        }
        return false; // Customer not found
    }

    public boolean verifyLogin(String identificationNo, String password) {
        Optional<Customer> customerOptional = customerRepository.findByIdentificationNo(identificationNo);
        if (customerOptional.isPresent()) {
            Customer customer = customerOptional.get();
            if (!"ACTIVE".equalsIgnoreCase(customer.getStatus())) {
                return false;
            }
            try{
                return verifyPassword(password, customer.getPassword());
            } catch(Exception e)  {
                return false;
            }
        }
        return false;
    }

    /**
     * Hashes the password using SHA-256.
     */
    public String hashPassword(String password) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] hashedBytes = md.digest(password.getBytes());
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Verify the password by comparing the raw password with the hashed password.
     */
    private boolean verifyPassword(String rawPassword, String storedHashedPassword) throws NoSuchAlgorithmException {
        return hashPassword(rawPassword).equals(storedHashedPassword);
    }
}
