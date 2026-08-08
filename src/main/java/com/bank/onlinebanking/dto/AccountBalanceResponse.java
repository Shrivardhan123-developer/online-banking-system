package com.bank.onlinebanking.dto;

import java.math.BigDecimal;

public class AccountBalanceResponse {

    private String accountNumber;
    private BigDecimal amountDeposited;
    private BigDecimal balance;
    private String message;

    public AccountBalanceResponse() {
    }

    public AccountBalanceResponse(
            String accountNumber,
            BigDecimal amountDeposited,
            BigDecimal balance,
            String message) {

        this.accountNumber = accountNumber;
        this.amountDeposited = amountDeposited;
        this.balance = balance;
        this.message = message;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BigDecimal getAmountDeposited() {
        return amountDeposited;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public String getMessage() {
        return message;
    }
}