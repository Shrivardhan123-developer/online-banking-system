package com.bank.onlinebanking.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.bank.onlinebanking.dto.TransactionResponse;
import com.bank.onlinebanking.entity.Customer;
import com.bank.onlinebanking.entity.Transaction;
import com.bank.onlinebanking.exception.InvalidTransactionException;
import com.bank.onlinebanking.repository.TransactionRepository;
import com.bank.onlinebanking.util.DtoMapper;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountService accountService;

    public TransactionService(
            TransactionRepository transactionRepository,
            AccountService accountService) {

        this.transactionRepository = transactionRepository;
        this.accountService = accountService;
    }

    // =====================================================
    // PAGINATED, FILTERED HISTORY FOR THE CURRENT CUSTOMER
    // Filters: type (DEPOSIT / WITHDRAW / TRANSFER_IN / TRANSFER_OUT),
    // status, optional date range.
    // =====================================================

    public Page<TransactionResponse> getCustomerTransactions(
            Long customerId,
            String type,
            String status,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable) {

        Page<Transaction> page = transactionRepository.searchForCustomer(
                customerId,
                blankToNull(type),
                blankToNull(status),
                fromDate,
                toDate,
                pageable);

        return page.map(DtoMapper::toTransactionResponse);
    }

    // =====================================================
    // FULL HISTORY FOR ONE ACCOUNT (newest first)
    // Customers may only read their own account history;
    // admins may read any account. Ownership is enforced here,
    // never trusted from the caller.
    // =====================================================

    public List<TransactionResponse> getAccountHistory(
            Customer customer, String accountNumber) {

        if (customer == null) {
            throw new InvalidTransactionException(
                    "Authenticated user is required");
        }

        if (!"ADMIN".equalsIgnoreCase(customer.getRole())) {
            // Throws 403 FORBIDDEN when the account is not owned
            // by the authenticated customer.
            accountService.getOwnedAccount(customer, accountNumber);
        }

        return transactionRepository
                .findByAccountAccountNumberOrderByTransactionDateDesc(
                        accountNumber)
                .stream()
                .map(DtoMapper::toTransactionResponse)
                .toList();
    }

    // =====================================================
    // RECENT TRANSACTIONS FOR THE DASHBOARD
    // =====================================================

    public List<TransactionResponse> getRecentForCustomer(Long customerId, int limit) {

        Pageable pageable = PageRequest.of(
                0, limit, Sort.by(Sort.Direction.DESC, "transactionDate"));

        return transactionRepository
                .searchForCustomer(customerId, null, null, null, null, pageable)
                .getContent()
                .stream()
                .map(DtoMapper::toTransactionResponse)
                .toList();
    }

    // =====================================================
    // HELPERS
    // =====================================================

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}