package com.wif3006.banking_system.base;

import com.wif3006.banking_system.deposit.dto.CreateDepositAccountDto;
import com.wif3006.banking_system.deposit.dto.DepositFundsDto;
import com.wif3006.banking_system.deposit.dto.GetDepositAccountDto;
import com.wif3006.banking_system.deposit.dto.UpdateDepositStatusDto;
import com.wif3006.banking_system.deposit.dto.WithdrawFundsDto;

public interface DepositAccountService {
    void createAccount(CreateDepositAccountDto createDepositAccountDto);
    GetDepositAccountDto getAccount(String identificationNo, String password);
    void closeAccount(String identificationNo, String password);
    GetDepositAccountDto depositFunds(DepositFundsDto depositFundsDto);
    GetDepositAccountDto withdrawFunds(WithdrawFundsDto withdrawFundsDto);
    GetDepositAccountDto updateStatus(UpdateDepositStatusDto updateDepositStatusDto);
}