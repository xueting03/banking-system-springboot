package com.wif3006.banking_system.customer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wif3006.banking_system.customer.dto.CreateCustomerDto;
import com.wif3006.banking_system.customer.dto.GetCustomerDto;
import com.wif3006.banking_system.customer.dto.UpdateCustomerDto;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerManagement customerService;

    /**
     * Create a new customer profile.
     */
    @PostMapping("/create")
    public ResponseEntity<String> createProfile(@RequestBody CreateCustomerDto customerDto) {
        try {
            customerService.createProfile(customerDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Customer profile created successfully!");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error creating profile: " + e.getMessage());
        }
    }

    /**
     * Retrieve customer profile by ID.
     */
    @GetMapping("/{identificationNo}")
    public ResponseEntity<?> getProfile(@PathVariable String identificationNo) {
        GetCustomerDto customerDto = customerService.getProfile(identificationNo);
        if (customerDto != null) {
            return ResponseEntity.ok(customerDto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found.");
        }
    }

    /**
     * Update an existing customer profile.
     */
    @PatchMapping("/update/{identificationNo}")
    public ResponseEntity<String> updateProfile(@PathVariable String identificationNo,
                                                @RequestBody UpdateCustomerDto updatedCustomerDto) {
        boolean updated = false;
        try {
            updated = customerService.updateProfile(identificationNo, updatedCustomerDto);
            if (updated) {
                return ResponseEntity.ok("Customer profile updated successfully!");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect current password or customer not found.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error updating profile: " + e.getMessage());
        }
    }
}
