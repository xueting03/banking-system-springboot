package com.wif3006.banking_system.service;

import com.wif3006.banking_system.dto.deposit.CreateDepositAccountDto;
import com.wif3006.banking_system.dto.deposit.DepositFundsDto;
import com.wif3006.banking_system.dto.deposit.GetDepositAccountDto;
import com.wif3006.banking_system.dto.deposit.UpdateDepositStatusDto;
import com.wif3006.banking_system.dto.deposit.WithdrawFundsDto;

public interface DepositAccountService {
    void createAccount(CreateDepositAccountDto createDepositAccountDto);
    GetDepositAccountDto getAccount(String identificationNo, String password);
    void closeAccount(String identificationNo, String password);
    GetDepositAccountDto depositFunds(DepositFundsDto depositFundsDto);
    GetDepositAccountDto withdrawFunds(WithdrawFundsDto withdrawFundsDto);
    GetDepositAccountDto updateStatus(UpdateDepositStatusDto updateDepositStatusDto);
}
