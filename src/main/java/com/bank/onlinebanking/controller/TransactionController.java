package com.bank.onlinebanking.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.onlinebanking.dto.TransactionResponse;
import com.bank.onlinebanking.entity.Customer;
import com.bank.onlinebanking.service.CustomerService;
import com.bank.onlinebanking.service.TransactionService;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;
    private final CustomerService customerService;

    public TransactionController(
            TransactionService transactionService,
            CustomerService customerService) {

        this.transactionService = transactionService;
        this.customerService = customerService;
    }

    // =====================================================
    // GET TRANSACTIONS BY ACCOUNT NUMBER
    // All business logic (ownership + history) lives in
    // TransactionService. This controller stays thin.
    // =====================================================

    @GetMapping("/{accountNumber}")
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @PathVariable String accountNumber) {

        Customer current =
                customerService.getCurrentCustomer();

        return ResponseEntity.ok(
                transactionService.getAccountHistory(
                        current, accountNumber));
    }
}