package com.bank.onlinebanking.controller;

import com.bank.onlinebanking.entity.Account;
import com.bank.onlinebanking.service.AccountService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PostMapping
    public ResponseEntity<Account> createAccount(
            @RequestParam Long customerId,
            @RequestParam String accountType) {

        Account account = accountService.createAccount(customerId, accountType);

        return new ResponseEntity<>(account, HttpStatus.CREATED);
    }
}