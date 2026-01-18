package com.wif3006.banking_system;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.lang.reflect.Field;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import com.wif3006.banking_system.base.CustomerService;
import com.wif3006.banking_system.base.model.Customer;
import com.wif3006.banking_system.base.model.SupportTicket;
import com.wif3006.banking_system.base.model.SupportTicket.Status;
import com.wif3006.banking_system.customer.dto.GetCustomerDto;
import com.wif3006.banking_system.customer_support.CustomerSupportManagement;
import com.wif3006.banking_system.customer_support.CustomerSupportRepository;
import com.wif3006.banking_system.customer_support.dto.AssignTicketRequestDto;
import com.wif3006.banking_system.customer_support.dto.CreateSupportTicketRequestDto;
import com.wif3006.banking_system.customer_support.dto.UpdateSupportTicketRequestDto;
import com.wif3006.banking_system.customer_support.dto.UpdateTicketStatusRequestDto;

public class CustomerSupportManagementTests {

    private CustomerSupportManagement customerSupportManagement;
    private CustomerSupportRepository customerSupportRepository;
    private CustomerService customerService;

    @BeforeEach
    public void setUp() throws Exception {
        customerSupportRepository = Mockito.mock(CustomerSupportRepository.class);
        customerService = Mockito.mock(CustomerService.class);
        customerSupportManagement = new CustomerSupportManagement();

        Field repositoryField = CustomerSupportManagement.class.getDeclaredField("customerSupportRepository");
        repositoryField.setAccessible(true);
        repositoryField.set(customerSupportManagement, customerSupportRepository);

        Field serviceField = CustomerSupportManagement.class.getDeclaredField("customerService");
        serviceField.setAccessible(true);
        serviceField.set(customerSupportManagement, customerService);
    }

    @Test
    public void testCreateTicket() {
        CreateSupportTicketRequestDto dto = new CreateSupportTicketRequestDto();
        dto.setCustomerIdNumber("021023-08-1925");
        dto.setAuthPassword("password");
        dto.setSubject("Issue with account");
        dto.setMessage("Description of the issue");

        Mockito.when(customerService.verifyLogin("021023-08-1925", "password")).thenReturn(true);

        GetCustomerDto customerDto = new GetCustomerDto();
        UUID customerId = UUID.randomUUID();
        customerDto.setId(customerId);
        customerDto.setIdentificationNo("021023-08-1925");
        Mockito.when(customerService.getProfile("021023-08-1925")).thenReturn(customerDto);

        customerSupportManagement.openTicket(dto);

        Mockito.verify(customerSupportRepository, Mockito.times(1)).save(Mockito.any(SupportTicket.class));
    }

    @Test
    public void testCreateTicketInvalidLogin() {
        CreateSupportTicketRequestDto dto = new CreateSupportTicketRequestDto();
        dto.setCustomerIdNumber("021023-08-1925");
        dto.setAuthPassword("wrongPassword");
        dto.setSubject("Issue with account");
        dto.setMessage("Description of the issue");

        Mockito.when(customerService.verifyLogin("021023-08-1925", "wrongPassword")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> customerSupportManagement.openTicket(dto));
    }

    @Test
    public void testUpdateTicketDetails() {
        UpdateSupportTicketRequestDto dto = new UpdateSupportTicketRequestDto();
        dto.setTicketId(UUID.randomUUID().toString());
        dto.setAuthPassword("password");
        dto.setSubject("Updated title");
        dto.setMessage("Updated description");

        SupportTicket ticket = new SupportTicket();
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setIdentificationNo("021023-08-1925");
        ticket.setCustomer(customer);
        ticket.setTitle("Original title");
        ticket.setDescription("Original description");
        ticket.setStatus(Status.OPEN);

        Mockito.when(customerSupportRepository.findById(UUID.fromString(dto.getTicketId())))
                .thenReturn(Optional.of(ticket));
        Mockito.when(customerService.verifyLogin("021023-08-1925", "password")).thenReturn(true);

        customerSupportManagement.reviseTicketDetails(dto);

        assertEquals("Updated title", ticket.getTitle());
        assertEquals("Updated description", ticket.getDescription());
        Mockito.verify(customerSupportRepository, Mockito.times(1)).save(ticket);
    }

    @Test
    public void testUpdateTicketDetailsInvalidLogin() {
        UpdateSupportTicketRequestDto dto = new UpdateSupportTicketRequestDto();
        dto.setTicketId(UUID.randomUUID().toString());
        dto.setAuthPassword("wrongPassword");

        SupportTicket ticket = new SupportTicket();
        Customer customer = new Customer();
        customer.setId(UUID.randomUUID());
        customer.setIdentificationNo("021023-08-1925");
        ticket.setCustomer(customer);
        ticket.setStatus(Status.OPEN);

        Mockito.when(customerSupportRepository.findById(UUID.fromString(dto.getTicketId())))
                .thenReturn(Optional.of(ticket));
        Mockito.when(customerService.verifyLogin("021023-08-1925", "wrongPassword")).thenReturn(false);

        assertThrows(IllegalArgumentException.class,
                () -> customerSupportManagement.reviseTicketDetails(dto));
    }

    @Test
    public void testAssignTicket() {
        AssignTicketRequestDto dto = new AssignTicketRequestDto();
        dto.setTicketId(UUID.randomUUID().toString());
        dto.setAssigneeId("staff-123");

        SupportTicket ticket = new SupportTicket();
        ticket.setStatus(Status.OPEN);

        Mockito.when(customerSupportRepository.findById(UUID.fromString(dto.getTicketId())))
                .thenReturn(Optional.of(ticket));

        customerSupportManagement.allocateTicket(dto);

        ArgumentCaptor<SupportTicket> captor = ArgumentCaptor.forClass(SupportTicket.class);
        Mockito.verify(customerSupportRepository).save(captor.capture());
        assertEquals("staff-123", captor.getValue().getAssignedStaffId());
    }

    @Test
    public void testUpdateTicketStatus() {
        UpdateTicketStatusRequestDto dto = new UpdateTicketStatusRequestDto();
        dto.setTicketId(UUID.randomUUID().toString());
        dto.setStatus(Status.IN_PROGRESS);
        dto.setActionedBy("staff-777");

        SupportTicket ticket = new SupportTicket();
        ticket.setStatus(Status.OPEN);
        ticket.setAssignedStaffId("staff-777");

        Mockito.when(customerSupportRepository.findById(UUID.fromString(dto.getTicketId())))
                .thenReturn(Optional.of(ticket));

        customerSupportManagement.changeTicketStatus(dto);

        ArgumentCaptor<SupportTicket> captor = ArgumentCaptor.forClass(SupportTicket.class);
        Mockito.verify(customerSupportRepository).save(captor.capture());
        assertEquals(Status.IN_PROGRESS, captor.getValue().getStatus());
    }
}
