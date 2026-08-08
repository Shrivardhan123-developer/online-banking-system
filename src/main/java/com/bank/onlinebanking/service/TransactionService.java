package com.bank.onlinebanking.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.bank.onlinebanking.entity.Transaction;
import com.bank.onlinebanking.repository.AccountRepository;
import com.bank.onlinebanking.repository.TransactionRepository;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // =========================
    // GET TRANSACTION HISTORY
    // =========================

    public List<Transaction> getTransactionHistory(Long accountId) {

        accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return transactionRepository
                .findByAccountIdOrderByTransactionDateDesc(accountId);
    }
}