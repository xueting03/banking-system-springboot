package com.wif3006.banking_system;

import com.wif3006.banking_system.dto.customer.CreateCustomerDto;
import com.wif3006.banking_system.repository.CustomerRepository;
import com.wif3006.banking_system.service.CustomerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class CustomerSimpleIntegrationTest {

    @Test
    public void testUpdateOnlyAddress() throws Exception {
        CreateCustomerDto dto = new CreateCustomerDto();
        dto.setName("Eve");
        dto.setIdentificationNo("ID0005");
        dto.setPassword("password5");
        dto.setAddress("5 Main Street");
        dto.setPhoneNo("5556667777");
        customerService.createProfile(dto);

        var customerOpt = customerRepository.findByIdentificationNo("ID0005");
        assertTrue(customerOpt.isPresent());
        var customer = customerOpt.get();
        customer.setAddress("5 New Avenue");
        customerRepository.save(customer);

        var updated = customerRepository.findByIdentificationNo("ID0005");
        assertTrue(updated.isPresent());
        assertEquals("5 New Avenue", updated.get().getAddress());
        assertEquals("5556667777", updated.get().getPhoneNo());
    }

    @Test
    public void testUpdateOnlyPhoneNo() throws Exception {
        CreateCustomerDto dto = new CreateCustomerDto();
        dto.setName("Frank");
        dto.setIdentificationNo("ID0006");
        dto.setPassword("password6");
        dto.setAddress("6 Main Street");
        dto.setPhoneNo("6667778888");
        customerService.createProfile(dto);

        var customerOpt = customerRepository.findByIdentificationNo("ID0006");
        assertTrue(customerOpt.isPresent());
        var customer = customerOpt.get();
        customer.setPhoneNo("9998887777");
        customerRepository.save(customer);

        var updated = customerRepository.findByIdentificationNo("ID0006");
        assertTrue(updated.isPresent());
        assertEquals("6 Main Street", updated.get().getAddress());
        assertEquals("9998887777", updated.get().getPhoneNo());
    }

    @Test
    public void testFindNonExistentCustomer() {
        var customer = customerRepository.findByIdentificationNo("ID9999");
        assertTrue(customer.isEmpty());
    }

    @Test
    public void testLoginNonExistentCustomer() {
        boolean login = customerService.verifyLogin("ID9999", "anyPassword");
        assertFalse(login);
    }

    @Test
    public void testUpdateCustomer() throws Exception {
        CreateCustomerDto dto = new CreateCustomerDto();
        dto.setName("Carol");
        dto.setIdentificationNo("ID0003");
        dto.setPassword("password3");
        dto.setAddress("3 Main Street");
        dto.setPhoneNo("1112223333");
        customerService.createProfile(dto);

        // Simulate update: change address and phone
        var customerOpt = customerRepository.findByIdentificationNo("ID0003");
        assertTrue(customerOpt.isPresent());
        var customer = customerOpt.get();
        customer.setAddress("3 New Street");
        customer.setPhoneNo("4445556666");
        customerRepository.save(customer);

        var updated = customerRepository.findByIdentificationNo("ID0003");
        assertTrue(updated.isPresent());
        assertEquals("3 New Street", updated.get().getAddress());
        assertEquals("4445556666", updated.get().getPhoneNo());
    }

    @Test
    public void testLoginCustomer() throws Exception {
        CreateCustomerDto dto = new CreateCustomerDto();
        dto.setName("Dave");
        dto.setIdentificationNo("ID0004");
        dto.setPassword("password4");
        dto.setAddress("4 Main Street");
        dto.setPhoneNo("2223334444");
        customerService.createProfile(dto);

        boolean loginSuccess = customerService.verifyLogin("ID0004", "password4");
        assertTrue(loginSuccess);

        boolean loginFail = customerService.verifyLogin("ID0004", "wrongpassword");
        assertFalse(loginFail);
    }

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerRepository customerRepository;

    @Test
    public void testCreateAndFindCustomer() throws Exception {
        CreateCustomerDto dto = new CreateCustomerDto();
        dto.setName("Alice");
        dto.setIdentificationNo("ID0001");
        dto.setPassword("password1");
        dto.setAddress("1 Main Street");
        dto.setPhoneNo("0123456789");

        customerService.createProfile(dto);

        var customer = customerRepository.findByIdentificationNo("ID0001");
        assertTrue(customer.isPresent());
        assertEquals("Alice", customer.get().getName());
        assertEquals("ID0001", customer.get().getIdentificationNo());
        assertEquals("1 Main Street", customer.get().getAddress());
        assertEquals("0123456789", customer.get().getPhoneNo());
    }
}
