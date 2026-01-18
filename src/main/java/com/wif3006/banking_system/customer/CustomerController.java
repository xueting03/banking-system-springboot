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
import com.wif3006.banking_system.customer.dto.CustomerStatusUpdateDto;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    @Autowired
    private CustomerImplementation customerService;

    // Register a customer account
    @PostMapping("/create")
    public ResponseEntity<String> registerCustomer(@RequestBody CreateCustomerDto dto) {
        try {
            customerService.createProfile(dto);
            return ResponseEntity.status(HttpStatus.CREATED).body("Account registration successful.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to register account: " + e.getMessage());
        }
    }

    // Fetch customer details using identification number
    @GetMapping("/{identificationNo}")
    public ResponseEntity<?> fetchCustomer(@PathVariable String identificationNo) {
        GetCustomerDto dto = customerService.getProfile(identificationNo);
        if (dto != null) {
            return ResponseEntity.ok(dto);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No customer record found.");
        }
    }

    // Modify customer information
    @PatchMapping("/update/{identificationNo}")
    public ResponseEntity<String> modifyCustomer(@PathVariable String identificationNo,
                                                 @RequestBody UpdateCustomerDto updateDto) {
        boolean result = false;
        try {
            result = customerService.updateProfile(identificationNo, updateDto);
            if (result) {
                return ResponseEntity.ok("Customer information has been updated.");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Update failed: invalid password or customer missing.");
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to update customer: " + e.getMessage());
        }
    }
    // Change customer account status (activate/deactivate)
    @PatchMapping("/status/{identificationNo}")
    public ResponseEntity<String> changeCustomerStatus(@PathVariable String identificationNo,
                                                      @RequestBody CustomerStatusUpdateDto statusDto) {
        try {
            statusDto.setIdentificationNo(identificationNo);
            boolean result = customerService.updateStatus(statusDto);
            if (result) {
                return ResponseEntity.ok("Customer status updated to " + statusDto.getStatus() + ".");
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Customer not found.");
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Unable to update status: " + e.getMessage());
        }
    }
}
