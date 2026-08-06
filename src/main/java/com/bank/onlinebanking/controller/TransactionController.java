package com.bank.onlinebanking.controller;

import com.bank.onlinebanking.entity.Transaction;
import com.bank.onlinebanking.service.TransactionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // Deposit
    @PostMapping("/deposit")
    public ResponseEntity<Transaction> deposit(
            @RequestParam Long accountId,
            @RequestParam BigDecimal amount) {

        Transaction transaction =
                transactionService.deposit(accountId, amount);

        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    // Withdraw
    @PostMapping("/withdraw")
    public ResponseEntity<Transaction> withdraw(
            @RequestParam Long accountId,
            @RequestParam BigDecimal amount) {

        Transaction transaction =
                transactionService.withdraw(accountId, amount);

        return new ResponseEntity<>(transaction, HttpStatus.CREATED);
    }

    // Transaction History
    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Transaction>> getTransactionHistory(
            @PathVariable Long accountId) {

        List<Transaction> transactions =
                transactionService.getTransactionHistory(accountId);

        return ResponseEntity.ok(transactions);
    }
}