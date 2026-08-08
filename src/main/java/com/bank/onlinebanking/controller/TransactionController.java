package com.bank.onlinebanking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.onlinebanking.entity.Transaction;
import com.bank.onlinebanking.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    // =========================
    // GET TRANSACTION HISTORY
    // =========================

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<Transaction>> getTransactionHistory(
            @PathVariable Long accountId) {

        List<Transaction> transactions =
                transactionService.getTransactionHistory(accountId);

        return ResponseEntity.ok(transactions);
    }
}