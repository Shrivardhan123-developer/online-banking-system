package com.bank.onlinebanking.util;

import java.util.List;
import java.util.stream.Collectors;

import com.bank.onlinebanking.dto.AccountResponse;
import com.bank.onlinebanking.dto.CustomerResponse;
import com.bank.onlinebanking.dto.TransactionResponse;
import com.bank.onlinebanking.entity.Account;
import com.bank.onlinebanking.entity.Customer;
import com.bank.onlinebanking.entity.Transaction;

/**
 * Central place that maps entities to response DTOs so that internal
 * fields such as passwords or hashes are never serialised.
 */
public final class DtoMapper {

    private DtoMapper() {
    }

    public static TransactionResponse toTransactionResponse(Transaction t) {
        return new TransactionResponse(
                t.getId(),
                t.getTransactionReference(),
                t.getAmount(),
                t.getType(),
                t.getStatus(),
                t.getSourceAccount(),
                t.getDestinationAccount(),
                t.getBalanceAfterTransaction(),
                t.getDescription(),
                t.getTransactionDate(),
                t.getAccount() == null ? null : t.getAccount().getAccountNumber()
        );
    }

    public static List<TransactionResponse> toTransactionResponses(
            List<Transaction> transactions) {

        return transactions.stream()
                .map(DtoMapper::toTransactionResponse)
                .collect(Collectors.toList());
    }

    public static AccountResponse toAccountResponse(Account a) {
        return new AccountResponse(
                a.getId(),
                a.getAccountNumber(),
                a.getAccountType(),
                a.getBalance(),
                a.getStatus(),
                a.getCustomer().getId(),
                a.getCustomer().getFullName(),
                a.getCustomer().getEmail()
        );
    }

    public static List<AccountResponse> toAccountResponses(List<Account> accounts) {
        return accounts.stream()
                .map(DtoMapper::toAccountResponse)
                .collect(Collectors.toList());
    }

    public static CustomerResponse toCustomerResponse(
            Customer c, List<Account> accounts) {

        return new CustomerResponse(
                c.getId(),
                c.getEmail(),
                c.getFullName(),
                c.getPhone(),
                c.getRole(),
                c.getStatus(),
                toAccountResponses(accounts == null ? List.of() : accounts)
        );
    }
}