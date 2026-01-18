package com.wif3006.banking_system.deposit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.wif3006.banking_system.base.DepositAccountService;
import com.wif3006.banking_system.deposit.dto.CloseDepositAccountDto;
import com.wif3006.banking_system.deposit.dto.CreateDepositAccountDto;
import com.wif3006.banking_system.deposit.dto.DepositFundsDto;
import com.wif3006.banking_system.deposit.dto.GetDepositAccountPayloadDto;
import com.wif3006.banking_system.deposit.dto.UpdateDepositStatusDto;
import com.wif3006.banking_system.deposit.dto.WithdrawFundsDto;

@RestController
@RequestMapping("/api/deposit-accounts")
public class DepositAccountController {

    @Autowired
    private DepositAccountService depositAccountService;

    @PostMapping("/create")
    public ResponseEntity<?> createAccount(@RequestBody CreateDepositAccountDto createDepositAccountDto) {
        try {
            depositAccountService.createAccount(createDepositAccountDto);
            return ResponseEntity.ok("Deposit account created.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("Invalid request: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body("Conflict: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("An error occurred while creating the deposit account: " + e.getMessage());
        }
    }

    @PostMapping("/get/{identificationNo}")
    public ResponseEntity<?> getAccount(@PathVariable String identificationNo, @RequestBody GetDepositAccountPayloadDto getDepositAccountPayloadDto) {
        try {
            return ResponseEntity.ok(depositAccountService.getAccount(identificationNo, getDepositAccountPayloadDto.getPassword()));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("Invalid request: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("An error occurred while retrieving the deposit account: " + e.getMessage());
        }
    }

    @PatchMapping("/close/{identificationNo}")
    public ResponseEntity<?> closeAccount(@PathVariable String identificationNo,
            @RequestBody CloseDepositAccountDto closeDepositAccountDto) {
        try {
            depositAccountService.closeAccount(identificationNo, closeDepositAccountDto.getPassword());
            return ResponseEntity.ok("Deposit account closed.");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("Invalid request: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body("Conflict: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("An error occurred while closing the deposit account: " + e.getMessage());
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> depositFunds(@RequestBody DepositFundsDto depositFundsDto) {
        try {
            return ResponseEntity.ok(depositAccountService.depositFunds(depositFundsDto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("Invalid request: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body("Conflict: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("An error occurred while depositing funds: " + e.getMessage());
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdrawFunds(@RequestBody WithdrawFundsDto withdrawFundsDto) {
        try {
            return ResponseEntity.ok(depositAccountService.withdrawFunds(withdrawFundsDto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("Invalid request: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body("Conflict: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("An error occurred while withdrawing funds: " + e.getMessage());
        }
    }

    @PatchMapping("/status")
    public ResponseEntity<?> updateStatus(@RequestBody UpdateDepositStatusDto updateDepositStatusDto) {
        try {
            return ResponseEntity.ok(depositAccountService.updateStatus(updateDepositStatusDto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(400).body("Invalid request: " + e.getMessage());
        } catch (IllegalStateException e) {
            return ResponseEntity.status(409).body("Conflict: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body("An error occurred while updating account status: " + e.getMessage());
        }
    }
}