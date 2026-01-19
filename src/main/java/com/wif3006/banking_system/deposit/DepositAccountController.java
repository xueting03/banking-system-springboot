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
import com.wif3006.banking_system.deposit.dto.UpdateDepositStatusDto;
import com.wif3006.banking_system.deposit.dto.VerifyAccountCredentialsDto;
import com.wif3006.banking_system.deposit.dto.WithdrawFundsDto;

@RestController
@RequestMapping("/api/deposit-accounts")
public class DepositAccountController {

    @Autowired
    private DepositAccountService depositAccountService;

    @PostMapping("/create")
    public ResponseEntity<?> createAccount(@RequestBody CreateDepositAccountDto requestDto) {
        try {
            depositAccountService.createAccount(requestDto);
            return ResponseEntity.ok("Account successfully created.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400).body("Request validation failed: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body("Operation conflict: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500)
                    .body("Failed to create deposit account: " + ex.getMessage());
        }
    }

    @PostMapping("/get/{identificationNo}")
    public ResponseEntity<?> getAccount(@PathVariable String identificationNo, @RequestBody VerifyAccountCredentialsDto authDto) {
        try {
            return ResponseEntity.ok(depositAccountService.getAccount(identificationNo, authDto.getPassword()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400).body("Validation error: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500)
                    .body("Unable to retrieve account information: " + ex.getMessage());
        }
    }

    @PatchMapping("/close/{identificationNo}")
    public ResponseEntity<?> closeAccount(@PathVariable String identificationNo,
            @RequestBody CloseDepositAccountDto closureRequest) {
        try {
            depositAccountService.closeAccount(identificationNo, closureRequest.getPassword());
            return ResponseEntity.ok("Account closure completed successfully.");
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400).body("Request processing error: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body("State conflict detected: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500)
                    .body("Account closure operation failed: " + ex.getMessage());
        }
    }

    @PostMapping("/deposit")
    public ResponseEntity<?> depositFunds(@RequestBody DepositFundsDto depositFundsDto) {
        try {
            return ResponseEntity.ok(depositAccountService.depositFunds(depositFundsDto));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400).body("Deposit validation failed: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body("Deposit conflict: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500)
                    .body("Deposit funds failed: " + ex.getMessage());
        }
    }

    @PostMapping("/withdraw")
    public ResponseEntity<?> withdrawFunds(@RequestBody WithdrawFundsDto withdrawalDto) {
        try {
            return ResponseEntity.ok(depositAccountService.withdrawFunds(withdrawalDto));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400).body("Withdrawal validation error: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body("Withdrawal state conflict: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500)
                    .body("Withdrawal operation error: " + ex.getMessage());
        }
    }

    @PatchMapping("/status")
    public ResponseEntity<?> updateStatus(@RequestBody UpdateDepositStatusDto statusUpdateDto) {
        try {
            return ResponseEntity.ok(depositAccountService.updateStatus(statusUpdateDto));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.status(400).body("Status update validation failed: " + ex.getMessage());
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(409).body("Status conflict detected: " + ex.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(500)
                    .body("Status update operation failed: " + ex.getMessage());
        }
    }
}