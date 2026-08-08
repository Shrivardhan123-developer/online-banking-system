package com.bank.onlinebanking.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TransactionResponse {

    private Long id;
    private String accountNumber;
    private BigDecimal amount;
    private String type;
    private String description;
    private LocalDateTime transactionDate;

    public TransactionResponse() {
    }

    public TransactionResponse(
            Long id,
            String accountNumber,
            BigDecimal amount,
            String type,
            String description,
            LocalDateTime transactionDate) {

        this.id = id;
        this.accountNumber = accountNumber;
        this.amount = amount;
        this.type = type;
        this.description = description;
        this.transactionDate = transactionDate;
    }

    public Long getId() {
        return id;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
}