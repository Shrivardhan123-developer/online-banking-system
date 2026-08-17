package com.bank.onlinebanking.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "transactions", indexes = {
        @Index(name = "idx_transaction_account", columnList = "account_id"),
        @Index(name = "idx_transaction_date", columnList = "transaction_date")
})
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_reference", unique = true, nullable = false, length = 40)
    private String transactionReference;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal amount;

    @Column(name = "transaction_type", nullable = false)
    private String type;

    /** COMPLETED / FAILED / PENDING */
    @Column(nullable = false)
    private String status = "COMPLETED";

    /** Account number for transfers; may be null for deposits/withdrawals. */
    @Column(name = "source_account")
    private String sourceAccount;

    /** Account number for transfers; may be null for deposits/withdrawals. */
    @Column(name = "destination_account")
    private String destinationAccount;

    @Column(name = "balance_after_transaction", precision = 18, scale = 2)
    private BigDecimal balanceAfterTransaction;

    @Column
    private String description;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    public Transaction() {
    }

    @PrePersist
    protected void onCreate() {
        if (this.transactionReference == null || this.transactionReference.isBlank()) {
            this.transactionReference = generateReference();
        }
        if (this.amount != null) {
            this.amount = this.amount.setScale(2, java.math.RoundingMode.HALF_UP);
        }
        if (this.transactionDate == null) {
            this.transactionDate = LocalDateTime.now();
        }
        this.createdAt = LocalDateTime.now();
    }

    public static String generateReference() {
        return "TXN" + UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 20)
                .toUpperCase();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public void setTransactionReference(String transactionReference) {
        this.transactionReference = transactionReference;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSourceAccount() {
        return sourceAccount;
    }

    public void setSourceAccount(String sourceAccount) {
        this.sourceAccount = sourceAccount;
    }

    public String getDestinationAccount() {
        return destinationAccount;
    }

    public void setDestinationAccount(String destinationAccount) {
        this.destinationAccount = destinationAccount;
    }

    public BigDecimal getBalanceAfterTransaction() {
        return balanceAfterTransaction;
    }

    public void setBalanceAfterTransaction(BigDecimal balanceAfterTransaction) {
        this.balanceAfterTransaction = balanceAfterTransaction;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}