package com.bank.onlinebanking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private String transactionReference;
    private BigDecimal amount;
    private String type;
    private String status;
    private String sourceAccount;
    private String destinationAccount;
    private BigDecimal balanceAfterTransaction;
    private String description;
    private LocalDateTime transactionDate;
    private String accountNumber;

    public TransactionResponse() {
    }

    public TransactionResponse(
            Long id,
            String transactionReference,
            BigDecimal amount,
            String type,
            String status,
            String sourceAccount,
            String destinationAccount,
            BigDecimal balanceAfterTransaction,
            String description,
            LocalDateTime transactionDate,
            String accountNumber) {

        this.id = id;
        this.transactionReference = transactionReference;
        this.amount = amount;
        this.type = type;
        this.status = status;
        this.sourceAccount = sourceAccount;
        this.destinationAccount = destinationAccount;
        this.balanceAfterTransaction = balanceAfterTransaction;
        this.description = description;
        this.transactionDate = transactionDate;
        this.accountNumber = accountNumber;
    }

    public Long getId() {
        return id;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getStatus() {
        return status;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public BigDecimal getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public String getAccountNumber() {
        return accountNumber;
    }
}