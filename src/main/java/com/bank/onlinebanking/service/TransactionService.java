package com.bank.onlinebanking.service;

import com.bank.onlinebanking.entity.Account;
import com.bank.onlinebanking.entity.Transaction;
import com.bank.onlinebanking.repository.AccountRepository;
import com.bank.onlinebanking.repository.TransactionRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class TransactionService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    public TransactionService(AccountRepository accountRepository,
                               TransactionRepository transactionRepository) {
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
    }

    // Deposit money
    public Transaction deposit(Long accountId, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Deposit amount must be greater than zero");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setType("DEPOSIT");
        transaction.setDescription("Cash deposit");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setAccount(account);

        return transactionRepository.save(transaction);
    }

    // Withdraw money
    public Transaction withdraw(Long accountId, BigDecimal amount) {

        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Withdrawal amount must be greater than zero");
        }

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setType("WITHDRAW");
        transaction.setDescription("Cash withdrawal");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setAccount(account);

        return transactionRepository.save(transaction);
    }

    // Get transaction history
    public List<Transaction> getTransactionHistory(Long accountId) {

        accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found"));

        return transactionRepository
                .findByAccountIdOrderByTransactionDateDesc(accountId);
    }
}