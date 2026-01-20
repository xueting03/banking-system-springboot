package com.wif3006.banking_system.service;

import com.wif3006.banking_system.dto.support.AssignTicketRequestDto;
import com.wif3006.banking_system.dto.support.CreateSupportTicketRequestDto;
import com.wif3006.banking_system.dto.support.UpdateSupportTicketRequestDto;
import com.wif3006.banking_system.dto.support.UpdateTicketStatusRequestDto;

public interface CustomerSupportService {
    void openTicket(CreateSupportTicketRequestDto createSupportTicketDto);

    void reviseTicketDetails(UpdateSupportTicketRequestDto updateSupportTicketDto);

    void allocateTicket(AssignTicketRequestDto assignTicketRequestDto);

    void changeTicketStatus(UpdateTicketStatusRequestDto updateTicketStatusRequestDto);
}
