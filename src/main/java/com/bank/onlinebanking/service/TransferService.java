package com.bank.onlinebanking.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.onlinebanking.dto.TransferResponse;
import com.bank.onlinebanking.entity.Account;
import com.bank.onlinebanking.entity.Transaction;
import com.bank.onlinebanking.exception.AccountNotActiveException;
import com.bank.onlinebanking.exception.InsufficientBalanceException;
import com.bank.onlinebanking.exception.InvalidTransactionException;
import com.bank.onlinebanking.exception.ResourceNotFoundException;
import com.bank.onlinebanking.exception.UnauthorizedOperationException;
import com.bank.onlinebanking.repository.AccountRepository;
import com.bank.onlinebanking.repository.TransactionRepository;

@Service
public class TransferService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final AuditService auditService;

    public TransferService(
            AccountRepository accountRepository,
            TransactionRepository transactionRepository,
            AuditService auditService) {

        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.auditService = auditService;
    }

    /**
     * Atomically moves funds from the sender's account to the receiver's
     * account. Debit and credit always succeed or fail together. The sender
     * account is validated against the authenticated customer id so that a
     * customer can never transfer from an account they do not own.
     */
    @Transactional
    public TransferResponse transfer(
            Long senderCustomerId,
            String senderAccountNumber,
            String receiverAccountNumber,
            BigDecimal amount,
            String description) {

        // -------------------------------------------------
        // 1. VALIDATE INPUTS
        // -------------------------------------------------

        if (senderAccountNumber == null || senderAccountNumber.isBlank()) {
            throw new InvalidTransactionException("Sender account number is required");
        }
        if (receiverAccountNumber == null || receiverAccountNumber.isBlank()) {
            throw new InvalidTransactionException("Receiver account number is required");
        }
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidTransactionException(
                    "Transfer amount must be greater than zero");
        }
        if (senderAccountNumber.equals(receiverAccountNumber)) {
            throw new InvalidTransactionException(
                    "Sender and receiver accounts cannot be the same");
        }

        // -------------------------------------------------
        // 2. LOCK BOTH ACCOUNTS (ordered, atomic)
        // -------------------------------------------------

        Account sender = findLocked(senderAccountNumber, "Sender");
        Account receiver = findLocked(receiverAccountNumber, "Receiver");

        // -------------------------------------------------
        // 3. OWNERSHIP + VALIDITY CHECKS
        // -------------------------------------------------

        if (senderCustomerId == null
                || !sender.getCustomer().getId().equals(senderCustomerId)) {
            throw new UnauthorizedOperationException(
                    "You can only transfer from an account you own");
        }

        requireActive(sender);
        requireActive(receiver);

        if (sender.getBalance().compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    "Insufficient balance in sender account");
        }

        // -------------------------------------------------
        // 4. DEBIT SENDER, CREDIT RECEIVER
        // -------------------------------------------------

        sender.setBalance(sender.getBalance().subtract(amount));
        receiver.setBalance(receiver.getBalance().add(amount));

        accountRepository.save(sender);
        accountRepository.save(receiver);
// -------------------------------------------------
        // 5. CREATE TRANSACTION RECORDS
        // -------------------------------------------------

        String effectiveDescription =
                (description == null || description.isBlank())
                        ? "Fund transfer to account " + receiverAccountNumber
                        : description.trim();

        String senderRef = Transaction.generateReference();
        String receiverRef = Transaction.generateReference();

        createTransferTransaction(sender, receiverAccountNumber, amount,
                "TRANSFER_OUT", effectiveDescription, senderRef, true);
        createTransferTransaction(receiver, senderAccountNumber, amount,
                "TRANSFER_IN", "Fund transfer from account " + senderAccountNumber,
                receiverRef, false);

        auditService.log("TRANSFER",
                "Transferred " + amount + " from " + senderAccountNumber
                        + " to " + receiverAccountNumber,
                sender.getCustomer().getEmail());

        // -------------------------------------------------
        // 6. RESPONSE
        // -------------------------------------------------

        return new TransferResponse(
                "Fund transfer successful",
                senderAccountNumber,
                receiverAccountNumber,
                amount,
                sender.getBalance(),
                receiver.getBalance()
        );
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private Account findLocked(String accountNumber, String label) {
        return accountRepository
                .findByAccountNumberForUpdate(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        label + " account not found: " + accountNumber));
    }

    private void createTransferTransaction(
            Account account,
            String counterpartyAccount,
            BigDecimal amount,
            String type,
            String description,
            String reference,
            boolean isSender) {

        Transaction transaction = new Transaction();
        transaction.setTransactionReference(reference);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setStatus("COMPLETED");
        transaction.setDescription(description);
        transaction.setTransactionDate(LocalDateTime.now());
        transaction.setAccount(account);
        transaction.setBalanceAfterTransaction(account.getBalance());

        if (isSender) {
            transaction.setSourceAccount(account.getAccountNumber());
            transaction.setDestinationAccount(counterpartyAccount);
        } else {
            transaction.setSourceAccount(counterpartyAccount);
            transaction.setDestinationAccount(account.getAccountNumber());
        }

        transactionRepository.save(transaction);
    }

    private void requireActive(Account account) {
        if (!"ACTIVE".equals(account.getStatus())) {
            throw new AccountNotActiveException(
                    "Account " + account.getAccountNumber() + " is not active");
        }
    }
}