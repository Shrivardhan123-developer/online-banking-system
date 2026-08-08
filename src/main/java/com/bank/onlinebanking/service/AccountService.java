package com.bank.onlinebanking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.onlinebanking.entity.Account;
import com.bank.onlinebanking.entity.Customer;
import com.bank.onlinebanking.entity.Transaction;
import com.bank.onlinebanking.repository.AccountRepository;
import com.bank.onlinebanking.repository.CustomerRepository;
import com.bank.onlinebanking.repository.TransactionRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final TransactionRepository transactionRepository;

    public AccountService(
            AccountRepository accountRepository,
            CustomerRepository customerRepository,
            TransactionRepository transactionRepository) {

        this.accountRepository = accountRepository;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    // =====================================================
    // CREATE ACCOUNT
    // =====================================================

    public Account createAccount(Long customerId, String accountType) {

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new RuntimeException("Customer not found"));

        Account account = new Account();

        account.setAccountNumber(generateAccountNumber());
        account.setAccountType(accountType);
        account.setBalance(BigDecimal.ZERO);
        account.setStatus("ACTIVE");
        account.setCustomer(customer);

        return accountRepository.save(account);
    }

    // =====================================================
    // FIND ACCOUNT
    // =====================================================

    public Account findByAccountNumber(String accountNumber) {

        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException("Account not found"));
    }

    // =====================================================
    // DEPOSIT MONEY
    // =====================================================

    @Transactional
    public Account deposit(String accountNumber, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(
                    "Deposit amount must be greater than zero");
        }

        Account account = findByAccountNumber(accountNumber);

        checkActiveAccount(account);

        BigDecimal newBalance =
                account.getBalance().add(amount);

        account.setBalance(newBalance);

        Account savedAccount = accountRepository.save(account);

        Transaction transaction = new Transaction();

        transaction.setAmount(amount);
        transaction.setType("DEPOSIT");
        transaction.setDescription("Cash deposit");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setAccount(savedAccount);

        transactionRepository.save(transaction);

        return savedAccount;
    }

    // =====================================================
    // WITHDRAW MONEY
    // =====================================================

    @Transactional
    public Account withdraw(String accountNumber, BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(
                    "Withdrawal amount must be greater than zero");
        }

        Account account = findByAccountNumber(accountNumber);

        checkActiveAccount(account);

        // Check sufficient balance
        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient balance");
        }

        BigDecimal newBalance =
                account.getBalance().subtract(amount);

        account.setBalance(newBalance);

        Account savedAccount = accountRepository.save(account);

        Transaction transaction = new Transaction();

        transaction.setAmount(amount);
        transaction.setType("WITHDRAW");
        transaction.setDescription("Cash withdrawal");
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setAccount(savedAccount);

        transactionRepository.save(transaction);

        return savedAccount;
    }

    // =====================================================
    // TRANSFER MONEY
    // =====================================================

    @Transactional
    public void transfer(
            String senderAccountNumber,
            String receiverAccountNumber,
            BigDecimal amount) {

        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException(
                    "Transfer amount must be greater than zero");
        }

        if (senderAccountNumber.equals(receiverAccountNumber)) {
            throw new RuntimeException(
                    "Sender and receiver accounts cannot be same");
        }

        Account sender =
                findByAccountNumber(senderAccountNumber);

        Account receiver =
                findByAccountNumber(receiverAccountNumber);

        checkActiveAccount(sender);
        checkActiveAccount(receiver);

        // Check sender balance
        if (sender.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException(
                    "Insufficient balance in sender account");
        }

        // =========================
        // Deduct from sender
        // =========================

        sender.setBalance(
                sender.getBalance().subtract(amount)
        );

        // =========================
        // Add to receiver
        // =========================

        receiver.setBalance(
                receiver.getBalance().add(amount)
        );

        accountRepository.save(sender);
        accountRepository.save(receiver);

        // =========================
        // Sender transaction
        // =========================

        Transaction senderTransaction = new Transaction();

        senderTransaction.setAmount(amount);
        senderTransaction.setType("TRANSFER_OUT");
        senderTransaction.setDescription(
                "Transfer to account " + receiverAccountNumber
        );
        senderTransaction.setTransactionDate(
                LocalDateTime.now()
        );
        senderTransaction.setAccount(sender);

        transactionRepository.save(senderTransaction);

        // =========================
        // Receiver transaction
        // =========================

        Transaction receiverTransaction = new Transaction();

        receiverTransaction.setAmount(amount);
        receiverTransaction.setType("TRANSFER_IN");
        receiverTransaction.setDescription(
                "Transfer from account " + senderAccountNumber
        );
        receiverTransaction.setTransactionDate(
                LocalDateTime.now()
        );
        receiverTransaction.setAccount(receiver);

        transactionRepository.save(receiverTransaction);
    }

    // =====================================================
    // CHECK ACCOUNT STATUS
    // =====================================================

    private void checkActiveAccount(Account account) {

        if (!"ACTIVE".equals(account.getStatus())) {
            throw new RuntimeException(
                    "Account is not active");
        }
    }

    // =====================================================
    // GENERATE ACCOUNT NUMBER
    // =====================================================

    private String generateAccountNumber() {

        Random random = new Random();

        long number =
                1000000000L + random.nextLong(9000000000L);

        return String.valueOf(number);
    }
}