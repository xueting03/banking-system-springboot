package com.wif3006.banking_system.base;

import com.wif3006.banking_system.customer_support.dto.CreateSupportTicketRequestDto;
import com.wif3006.banking_system.customer_support.dto.AssignTicketRequestDto;
import com.wif3006.banking_system.customer_support.dto.UpdateSupportTicketRequestDto;
import com.wif3006.banking_system.customer_support.dto.UpdateTicketStatusRequestDto;

public interface CustomerSupportService {
    void openTicket(CreateSupportTicketRequestDto createSupportTicketDto);

    void reviseTicketDetails(UpdateSupportTicketRequestDto updateSupportTicketDto);

    void allocateTicket(AssignTicketRequestDto assignTicketRequestDto);

    void changeTicketStatus(UpdateTicketStatusRequestDto updateTicketStatusRequestDto);
}
