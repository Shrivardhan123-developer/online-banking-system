package com.bank.onlinebanking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.onlinebanking.entity.Account;
import com.bank.onlinebanking.entity.Customer;
import com.bank.onlinebanking.entity.Transaction;
import com.bank.onlinebanking.exception.AccountNotActiveException;
import com.bank.onlinebanking.exception.InsufficientBalanceException;
import com.bank.onlinebanking.exception.InvalidTransactionException;
import com.bank.onlinebanking.exception.ResourceNotFoundException;
import com.bank.onlinebanking.exception.UnauthorizedOperationException;
import com.bank.onlinebanking.repository.AccountRepository;
import com.bank.onlinebanking.repository.TransactionRepository;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;

    public AccountService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            AuditService auditService) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.auditService = auditService;
    }

    // =====================================================
    // CREATE ACCOUNT
    // =====================================================

    @Transactional
    public Account createAccount(Customer customer, String accountType) {

        String type = normaliseType(accountType);

        if (customer == null) {
            throw new InvalidTransactionException("Customer is required");
        }

        Account account = new Account();
        account.setAccountNumber(generateUniqueAccountNumber());
        account.setAccountType(type);
        account.setBalance(BigDecimal.ZERO.setScale(2));
        account.setStatus("ACTIVE");
        account.setCustomer(customer);

        Account saved = accountRepository.save(account);

        auditService.log("ACCOUNT_CREATED",
                "Created " + type + " account " + saved.getAccountNumber(),
                customer.getEmail());

        return saved;
    }
// =====================================================
    // READ METHODS
    // =====================================================

    public List<Account> getAccountsForCustomer(Long customerId) {
        return accountRepository.findByCustomerId(customerId);
    }

    /**
     * Looks up an account and enforces that it belongs to the given
     * customer. Used to protect all customer-facing account endpoints.
     */
    public Account getOwnedAccount(Customer customer, String accountNumber) {

        Account account = accountRepository
                .findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with number: " + accountNumber));

        if (!account.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorizedOperationException(
                    "You do not have access to this account");
        }

        return account;
    }

    public Account findAccountByNumber(String accountNumber) {
        return accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with number: " + accountNumber));
    }

    public BigDecimal getBalance(Customer customer, String accountNumber) {
        return getOwnedAccount(customer, accountNumber).getBalance();
    }

    public List<Transaction> getRecentTransactions(Customer customer, int limit) {
        return getAccountsForCustomer(customer.getId()).stream()
                .findFirst()
                .map(account -> transactionRepository
                        .findByAccountAccountNumberOrderByTransactionDateDesc(
                                account.getAccountNumber()))
                .orElse(List.of())
                .stream()
                .limit(limit)
                .toList();
    }

    // =====================================================
    // DEPOSIT
    // =====================================================

    @Transactional
    public Account deposit(
            String accountNumber,
            BigDecimal amount,
            String description,
            Customer customer) {

        validateAmount(amount, "Deposit");

        Account account = findLockedForUpdate(accountNumber);

        ensureOwnership(customer, account);
        requireActive(account);

        account.setBalance(account.getBalance().add(amount));

        Account saved = accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setType("DEPOSIT");
        transaction.setDescription(
                (description == null || description.isBlank())
                        ? "Cash deposit"
                        : description.trim());
        transaction.setStatus("COMPLETED");
        transaction.setBalanceAfterTransaction(saved.getBalance());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setAccount(saved);

        transactionRepository.save(transaction);

        auditService.log("DEPOSIT",
                "Deposited " + amount + " to account " + accountNumber,
                customer.getEmail());

        return saved;
    }

    // =====================================================
    // WITHDRAW
    // =====================================================

    @Transactional
    public Account withdraw(
            String accountNumber,
            BigDecimal amount,
            String description,
            Customer customer) {

        validateAmount(amount, "Withdrawal");

        Account account = findLockedForUpdate(accountNumber);

        ensureOwnership(customer, account);
        requireActive(account);

        if (account.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance in account " + accountNumber);
        }

        account.setBalance(account.getBalance().subtract(amount));

        Account saved = accountRepository.save(account);

        Transaction transaction = new Transaction();
        transaction.setAmount(amount);
        transaction.setType("WITHDRAW");
        transaction.setDescription(
                (description == null || description.isBlank())
                        ? "Cash withdrawal"
                        : description.trim());
        transaction.setStatus("COMPLETED");
        transaction.setBalanceAfterTransaction(saved.getBalance());
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setAccount(saved);

        transactionRepository.save(transaction);

        auditService.log("WITHDRAW",
                "Withdrawn " + amount + " from account " + accountNumber,
                customer.getEmail());

        return saved;
    }
// =====================================================
    // HELPERS
    // =====================================================

    /**
     * Loads the account with a pessimistic write lock so two concurrent
     * withdrawals/transfers cannot both observe the same balance.
     */
    private Account findLockedForUpdate(String accountNumber) {
        return accountRepository
                .findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account not found with number: " + accountNumber));
    }

    private void validateAmount(BigDecimal amount, String operation) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException(
                    operation + " amount must be greater than zero");
        }
    }

    private void ensureOwnership(Customer customer, Account account) {
        if (!account.getCustomer().getId().equals(customer.getId())) {
            throw new UnauthorizedOperationException(
                    "You do not have access to this account");
        }
    }

    private void requireActive(Account account) {
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new AccountNotActiveException(
                    "Account " + account.getAccountNumber() + " is not active");
        }
    }

    private String normaliseType(String accountType) {
        String type = accountType == null ? "" : accountType.trim().toUpperCase();
        if (!type.equals("SAVINGS") && !type.equals("CURRENT")) {
            throw new InvalidTransactionException(
                    "Account type must be SAVINGS or CURRENT");
        }
        return type;
    }

    private String generateUniqueAccountNumber() {
        Random random = new Random();
        String number;
        do {
            number = String.valueOf(
                    1000000000L + random.nextLong(9000000000L));
        } while (accountRepository.findByAccountNumber(number).isPresent());
        return number;
    }
}