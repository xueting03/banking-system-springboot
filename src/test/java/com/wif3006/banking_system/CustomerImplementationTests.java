package com.wif3006.banking_system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Field;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import com.wif3006.banking_system.base.model.Customer;
import com.wif3006.banking_system.customer.CustomerImplementation;
import com.wif3006.banking_system.customer.CustomerRepository;
import com.wif3006.banking_system.customer.dto.CreateCustomerDto;
import com.wif3006.banking_system.customer.dto.GetCustomerDto;
import com.wif3006.banking_system.customer.dto.UpdateCustomerDto;
import com.wif3006.banking_system.customer.dto.CustomerStatusUpdateDto;

public class CustomerImplementationTests {
    @Test
    public void testCreateProfileWeakPassword() {
        CreateCustomerDto dto = new CreateCustomerDto();
        dto.setName("Jane Doe");
        dto.setIdentificationNo("030119-08-3006");
        dto.setPhoneNo("0183831234");
        dto.setAddress("456 Main St");
        dto.setPassword("123"); // Too short

        Exception thrown = null;
        try {
            customerManagement.createProfile(dto);
        } catch (Exception ex) {
            thrown = ex;
        }
        assertNotNull(thrown, "Exception should be thrown for weak password");
        assertTrue(thrown instanceof IllegalArgumentException, "Expected IllegalArgumentException");
    }

    @Test
    public void testUpdateProfileWeakPassword() throws NoSuchAlgorithmException {
        Customer user = new Customer();
        user.setId(UUID.randomUUID());
        user.setName("Sebastian Vettel");
        user.setIdentificationNo("030119-08-3006");
        user.setPhoneNo("0115160828");
        user.setAddress("214 Street Mary");
        user.setPassword(customerManagement.hashPassword("password"));

        Mockito.when(customerRepository.findByIdentificationNo("030119-08-3006")).thenReturn(Optional.of(user));

        UpdateCustomerDto updateDto = new UpdateCustomerDto();
        updateDto.setCurrentPassword("password");
        updateDto.setNewPassword("123"); // Too short

        Exception thrown = null;
        try {
            customerManagement.updateProfile("030119-08-3006", updateDto);
        } catch (Exception ex) {
            thrown = ex;
        }
        assertNotNull(thrown, "Exception expected for weak password update");
        assertTrue(thrown instanceof IllegalArgumentException, "Should throw IllegalArgumentException");
    }

    @Test
    public void testUpdateProfileWrongCurrentPassword() throws NoSuchAlgorithmException {
        Customer user = new Customer();
        user.setId(UUID.randomUUID());
        user.setName("Sebastian Vettel");
        user.setIdentificationNo("030119-08-3006");
        user.setPhoneNo("0115160828");
        user.setAddress("214 Street Mary");
        user.setPassword(customerManagement.hashPassword("password"));

        Mockito.when(customerRepository.findByIdentificationNo("030119-08-3006")).thenReturn(Optional.of(user));

        UpdateCustomerDto updateDto = new UpdateCustomerDto();
        updateDto.setCurrentPassword("wrongPassword");
        updateDto.setNewPassword("new123password23");

        boolean result = customerManagement.updateProfile("030119-08-3006", updateDto);
        assertFalse(result, "Update should fail with incorrect current password");
    }

    @Test
    public void testUpdateStatus_deactivateCustomer() {
        Customer user = new Customer();
        user.setId(UUID.randomUUID());
        user.setName("Sebastian Vettel");
        user.setIdentificationNo("030119-08-3006");
        user.setPhoneNo("0115160828");
        user.setAddress("214 Street Mary");
        user.setPassword("hashedPassword");
        user.setStatus("ACTIVE");

        Mockito.when(customerRepository.findByIdentificationNo("030119-08-3006")).thenReturn(Optional.of(user));

        CustomerStatusUpdateDto statusDto = new CustomerStatusUpdateDto();
        statusDto.setIdentificationNo("030119-08-3006");
        statusDto.setStatus("DEACTIVATED");

        boolean result = customerManagement.updateStatus(statusDto);
        Mockito.verify(customerRepository, Mockito.times(1)).save(Mockito.any(Customer.class));
        assertEquals("DEACTIVATED", user.getStatus(), "Status should be updated to DEACTIVATED");
    }

    private CustomerImplementation customerManagement;
    private CustomerRepository customerRepository;

    @BeforeEach
    public void setUp() throws Exception {
        customerRepository = Mockito.mock(CustomerRepository.class);
        customerManagement = new CustomerImplementation();

        // Inject the mock repository into customerManagement using reflection
            Field repositoryField = CustomerImplementation.class.getDeclaredField("repository");
        repositoryField.setAccessible(true);
        repositoryField.set(customerManagement, customerRepository);
    }

    @Test
    public void testCreateProfileValidData() throws NoSuchAlgorithmException {
        CreateCustomerDto dto = new CreateCustomerDto();
        dto.setName("Sebastian Vettel");
        dto.setIdentificationNo("030119-08-3006");
        dto.setPhoneNo("0115160828");
        dto.setAddress("214 Street Mary");
        dto.setPassword("123password");

        customerManagement.createProfile(dto);

        Mockito.verify(customerRepository, Mockito.times(1)).save(Mockito.any(Customer.class));
    }

    @Test
    public void testGetProfileExistingCustomer() {
        Customer user = new Customer();
        user.setId(UUID.randomUUID());
        user.setName("Sebastian Vettel");
        user.setIdentificationNo("030119-08-3006");
        user.setPhoneNo("0115160828");
        user.setAddress("214 Street Mary");
        user.setPassword("hashedPassword");

        Mockito.when(customerRepository.findByIdentificationNo("030119-08-3006")).thenReturn(Optional.of(user));

        GetCustomerDto dto = customerManagement.getProfile("030119-08-3006");

        assertNotNull(dto, "Profile should not be null");
        assertEquals("Sebastian Vettel", dto.getName(), "Name should match");
    }

    @Test
    public void testUpdateProfileValidData() throws NoSuchAlgorithmException {
        Customer user = new Customer();
        user.setId(UUID.randomUUID());
        user.setName("Sebastian Vettel");
        user.setIdentificationNo("030119-08-3006");
        user.setPhoneNo("0115160828");
        user.setAddress("214 Street Mary");
        user.setPassword(customerManagement.hashPassword("password"));

        Mockito.when(customerRepository.findByIdentificationNo("030119-08-3006")).thenReturn(Optional.of(user));

        UpdateCustomerDto updateDto = new UpdateCustomerDto();
        updateDto.setName("John Smith");
        updateDto.setCurrentPassword("password");
        updateDto.setNewPassword("newPass1");

        boolean result = customerManagement.updateProfile("030119-08-3006", updateDto);

        assertTrue(result, "Update should succeed");
        Mockito.verify(customerRepository, Mockito.times(1)).save(Mockito.any(Customer.class));
    }

    @Test
    public void testVerifyLoginValidCredentials() throws NoSuchAlgorithmException {
        Customer user = new Customer();
        user.setId(UUID.randomUUID());
        user.setName("Sebastian Vettel");
        user.setIdentificationNo("030119-08-3006");
        user.setPhoneNo("0115160828");
        user.setAddress("214 Street Mary");
        user.setPassword(customerManagement.hashPassword("password"));
        user.setStatus("ACTIVE");

        Mockito.when(customerRepository.findByIdentificationNo("030119-08-3006")).thenReturn(Optional.of(user));

        boolean loggedIn = customerManagement.verifyLogin("030119-08-3006", "password");

        assertTrue(loggedIn, "Login should succeed for active user");
    }

    @Test
    public void testVerifyLoginInvalidCredentials() throws NoSuchAlgorithmException {
        Customer user = new Customer();
        user.setId(UUID.randomUUID());
        user.setName("Sebastian Vettel");
        user.setIdentificationNo("030119-08-3006");
        user.setPhoneNo("0115160828");
        user.setAddress("214 Street Mary");
        user.setPassword(customerManagement.hashPassword("password"));
        user.setStatus("DEACTIVATED");

        Mockito.when(customerRepository.findByIdentificationNo("030119-08-3006")).thenReturn(Optional.of(user));

        boolean loggedIn1 = customerManagement.verifyLogin("030119-08-3006", "password");
        boolean loggedIn2 = customerManagement.verifyLogin("030119-08-3006", "wrongPassword");

        assertFalse(loggedIn1, "Login should fail for deactivated user");
        assertFalse(loggedIn2, "Login should fail with incorrect password");
    }
}