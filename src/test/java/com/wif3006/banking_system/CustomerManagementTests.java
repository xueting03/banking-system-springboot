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
import com.wif3006.banking_system.customer.CustomerManagement;
import com.wif3006.banking_system.customer.CustomerRepository;
import com.wif3006.banking_system.customer.dto.CreateCustomerDto;
import com.wif3006.banking_system.customer.dto.GetCustomerDto;
import com.wif3006.banking_system.customer.dto.UpdateCustomerDto;

public class CustomerManagementTests {
    @Test
    public void testCreateProfileWithWeakPassword() {
        CreateCustomerDto customerDto = new CreateCustomerDto();
        customerDto.setName("Jane Doe");
        customerDto.setIdentificationNo("021023-08-1926");
        customerDto.setPhoneNo("0183831234");
        customerDto.setAddress("456 Main St");
        customerDto.setPassword("123"); // Weak password

        Exception exception = null;
        try {
            customerManagement.createProfile(customerDto);
        } catch (Exception e) {
            exception = e;
        }
        assertNotNull(exception);
        assertTrue(exception instanceof IllegalArgumentException);
    }

    @Test
    public void testUpdateProfileWithWeakPassword() throws NoSuchAlgorithmException {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setName("John Doe");
        customer.setIdentificationNo("021023-08-1925");
        customer.setPhoneNo("0183831233");
        customer.setAddress("123 Main St");
        customer.setPassword(customerManagement.hashPassword("password"));

        Mockito.when(customerRepository.findByIdentificationNo("021023-08-1925")).thenReturn(Optional.of(customer));

        UpdateCustomerDto updateCustomerDto = new UpdateCustomerDto();
        updateCustomerDto.setCurrentPassword("password");
        updateCustomerDto.setNewPassword("123"); // Weak password

        Exception exception = null;
        try {
            customerManagement.updateProfile("021023-08-1925", updateCustomerDto);
        } catch (Exception e) {
            exception = e;
        }
        assertNotNull(exception);
        assertTrue(exception instanceof IllegalArgumentException);
    }

    @Test
    public void testUpdateProfileWithIncorrectCurrentPassword() throws NoSuchAlgorithmException {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setName("John Doe");
        customer.setIdentificationNo("021023-08-1925");
        customer.setPhoneNo("0183831233");
        customer.setAddress("123 Main St");
        customer.setPassword(customerManagement.hashPassword("password"));

        Mockito.when(customerRepository.findByIdentificationNo("021023-08-1925")).thenReturn(Optional.of(customer));

        UpdateCustomerDto updateCustomerDto = new UpdateCustomerDto();
        updateCustomerDto.setCurrentPassword("wrongPassword");
        updateCustomerDto.setNewPassword("newPassword123");

        boolean updated = customerManagement.updateProfile("021023-08-1925", updateCustomerDto);
        assertFalse(updated);
    }

    @Test
    public void testUpdateProfileStatusChange() throws NoSuchAlgorithmException {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setName("John Doe");
        customer.setIdentificationNo("021023-08-1925");
        customer.setPhoneNo("0183831233");
        customer.setAddress("123 Main St");
        customer.setPassword(customerManagement.hashPassword("password"));
        customer.setStatus("ACTIVE");

        Mockito.when(customerRepository.findByIdentificationNo("021023-08-1925")).thenReturn(Optional.of(customer));

        UpdateCustomerDto updateCustomerDto = new UpdateCustomerDto();
        updateCustomerDto.setCurrentPassword("password");
        updateCustomerDto.setStatus("DEACTIVATED");

        boolean updated = customerManagement.updateProfile("021023-08-1925", updateCustomerDto);
        Mockito.verify(customerRepository, Mockito.times(1)).save(Mockito.any(Customer.class));
        assertEquals("DEACTIVATED", customer.getStatus());
    }

    private CustomerManagement customerManagement;
    private CustomerRepository customerRepository;

    @BeforeEach
    public void setUp() throws Exception {
        customerRepository = Mockito.mock(CustomerRepository.class);
        customerManagement = new CustomerManagement();

        // Inject the mock repository into customerManagement using reflection
        Field repositoryField = CustomerManagement.class.getDeclaredField("customerRepository");
        repositoryField.setAccessible(true);
        repositoryField.set(customerManagement, customerRepository);
    }

    @Test
    public void testCreateProfile() throws NoSuchAlgorithmException {
        CreateCustomerDto customerDto = new CreateCustomerDto();
        customerDto.setName("John Doe");
        customerDto.setIdentificationNo("021023-08-1925");
        customerDto.setPhoneNo("0183831233");
        customerDto.setAddress("123 Main St");
        customerDto.setPassword("password");

        customerManagement.createProfile(customerDto);

        Mockito.verify(customerRepository, Mockito.times(1)).save(Mockito.any(Customer.class));
    }

    @Test
    public void testGetProfile() {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setName("John Doe");
        customer.setIdentificationNo("021023-08-1925");
        customer.setPhoneNo("0183831233");
        customer.setAddress("123 Main St");
        customer.setPassword("hashedPassword");

        Mockito.when(customerRepository.findByIdentificationNo("021023-08-1925")).thenReturn(Optional.of(customer));

        GetCustomerDto customerDto = customerManagement.getProfile("021023-08-1925");

        assertNotNull(customerDto);
        assertEquals("John Doe", customerDto.getName());
    }

    @Test
    public void testUpdateProfile() throws NoSuchAlgorithmException {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setName("John Doe");
        customer.setIdentificationNo("021023-08-1925");
        customer.setPhoneNo("0183831233");
        customer.setAddress("123 Main St");
        customer.setPassword(customerManagement.hashPassword("password"));

        Mockito.when(customerRepository.findByIdentificationNo("021023-08-1925")).thenReturn(Optional.of(customer));

        UpdateCustomerDto updateCustomerDto = new UpdateCustomerDto();
        updateCustomerDto.setName("John Smith");
        updateCustomerDto.setCurrentPassword("password");
        updateCustomerDto.setNewPassword("newPassword");

        boolean updated = customerManagement.updateProfile("021023-08-1925", updateCustomerDto);

        assertTrue(updated);
        Mockito.verify(customerRepository, Mockito.times(1)).save(Mockito.any(Customer.class));
    }

    @Test
    public void testVerifyLogin() throws NoSuchAlgorithmException {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setName("John Doe");
        customer.setIdentificationNo("021023-08-1925");
        customer.setPhoneNo("0183831233");
        customer.setAddress("123 Main St");
        customer.setPassword(customerManagement.hashPassword("password"));
        customer.setStatus("ACTIVE");

        Mockito.when(customerRepository.findByIdentificationNo("021023-08-1925")).thenReturn(Optional.of(customer));

        boolean loggedIn = customerManagement.verifyLogin("021023-08-1925", "password");

        assertTrue(loggedIn);
    }

    @Test
    public void testVerifyLoginDeactivated() throws NoSuchAlgorithmException {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setName("John Doe");
        customer.setIdentificationNo("021023-08-1925");
        customer.setPhoneNo("0183831233");
        customer.setAddress("123 Main St");
        customer.setPassword(customerManagement.hashPassword("password"));
        customer.setStatus("DEACTIVATED");

        Mockito.when(customerRepository.findByIdentificationNo("021023-08-1925")).thenReturn(Optional.of(customer));

        boolean loggedIn = customerManagement.verifyLogin("021023-08-1925", "password");

        assertFalse(loggedIn);
    }

    @Test
    public void testVerifyLoginInvalid() throws NoSuchAlgorithmException {
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setName("John Doe");
        customer.setIdentificationNo("021023-08-1925");
        customer.setPhoneNo("0183831233");
        customer.setAddress("123 Main St");
        customer.setPassword(customerManagement.hashPassword("password"));
        customer.setStatus("ACTIVE");

        Mockito.when(customerRepository.findByIdentificationNo("021023-08-1925")).thenReturn(Optional.of(customer));

        boolean loggedIn = customerManagement.verifyLogin("021023-08-1925", "wrongPassword");

        assertFalse(loggedIn);
    }
}