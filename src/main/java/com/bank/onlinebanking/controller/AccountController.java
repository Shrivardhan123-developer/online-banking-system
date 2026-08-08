package com.bank.onlinebanking.controller;

import java.math.BigDecimal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bank.onlinebanking.dto.AccountBalanceResponse;
import com.bank.onlinebanking.dto.AccountResponse;
import com.bank.onlinebanking.dto.DepositRequest;
import com.bank.onlinebanking.entity.Account;
import com.bank.onlinebanking.service.AccountService;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // =====================================================
    // CREATE ACCOUNT
    // POST /api/accounts
    // =====================================================

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @RequestParam Long customerId,
            @RequestParam String accountType) {

        Account account = accountService.createAccount(
                customerId,
                accountType
        );

        AccountResponse response = new AccountResponse(
                account.getId(),
                account.getAccountNumber(),
                account.getAccountType(),
                account.getBalance(),
                account.getStatus(),
                account.getCustomer().getId(),
                account.getCustomer().getFullName(),
                account.getCustomer().getEmail()
        );

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED
        );
    }

    // =====================================================
    // DEPOSIT MONEY
    // POST /api/accounts/deposit
    // =====================================================

    @PostMapping("/deposit")
    public ResponseEntity<AccountBalanceResponse> deposit(
            @RequestBody DepositRequest request) {

        Account account = accountService.deposit(
                request.getAccountNumber(),
                request.getAmount()
        );

        AccountBalanceResponse response =
                new AccountBalanceResponse(
                        account.getAccountNumber(),
                        request.getAmount(),
                        account.getBalance(),
                        "Money deposited successfully"
                );

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // WITHDRAW MONEY
    // POST /api/accounts/withdraw
    // =====================================================

    @PostMapping("/withdraw")
    public ResponseEntity<AccountBalanceResponse> withdraw(
            @RequestBody DepositRequest request) {

        Account account = accountService.withdraw(
                request.getAccountNumber(),
                request.getAmount()
        );

        AccountBalanceResponse response =
                new AccountBalanceResponse(
                        account.getAccountNumber(),
                        request.getAmount(),
                        account.getBalance(),
                        "Money withdrawn successfully"
                );

        return ResponseEntity.ok(response);
    }

    // =====================================================
    // TRANSFER MONEY
    // POST /api/accounts/transfer
    // =====================================================

    @PostMapping("/transfer")
    public ResponseEntity<String> transfer(
            @RequestParam String senderAccountNumber,
            @RequestParam String receiverAccountNumber,
            @RequestParam BigDecimal amount) {

        accountService.transfer(
                senderAccountNumber,
                receiverAccountNumber,
                amount
        );

        return ResponseEntity.ok(
                "Money transferred successfully"
        );
    }

    // =====================================================
    // GET ACCOUNT BALANCE
    // GET /api/accounts/{accountNumber}/balance
    // =====================================================

    @GetMapping("/{accountNumber}/balance")
    public ResponseEntity<AccountBalanceResponse> getBalance(
            @PathVariable String accountNumber) {

        Account account =
                accountService.findByAccountNumber(accountNumber);

        AccountBalanceResponse response =
                new AccountBalanceResponse(
                        account.getAccountNumber(),
                        BigDecimal.ZERO,
                        account.getBalance(),
                        "Account balance fetched successfully"
                );

        return ResponseEntity.ok(response);
    }
}