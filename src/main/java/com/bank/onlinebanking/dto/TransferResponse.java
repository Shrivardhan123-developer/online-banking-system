package com.bank.onlinebanking.dto;

import java.math.BigDecimal;

public class TransferResponse {

    private String message;

    private String senderAccountNumber;

    private String receiverAccountNumber;

    private BigDecimal amount;

    private BigDecimal senderBalance;

    private BigDecimal receiverBalance;


    // =====================================================
    // DEFAULT CONSTRUCTOR
    // =====================================================

    public TransferResponse() {
    }


    // =====================================================
    // PARAMETERIZED CONSTRUCTOR
    // =====================================================

    public TransferResponse(
            String message,
            String senderAccountNumber,
            String receiverAccountNumber,
            BigDecimal amount,
            BigDecimal senderBalance,
            BigDecimal receiverBalance) {

        this.message = message;
        this.senderAccountNumber = senderAccountNumber;
        this.receiverAccountNumber = receiverAccountNumber;
        this.amount = amount;
        this.senderBalance = senderBalance;
        this.receiverBalance = receiverBalance;
    }


    // =====================================================
    // GETTERS & SETTERS
    // =====================================================

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


    public String getSenderAccountNumber() {
        return senderAccountNumber;
    }

    public void setSenderAccountNumber(String senderAccountNumber) {
        this.senderAccountNumber = senderAccountNumber;
    }


    public String getReceiverAccountNumber() {
        return receiverAccountNumber;
    }

    public void setReceiverAccountNumber(String receiverAccountNumber) {
        this.receiverAccountNumber = receiverAccountNumber;
    }


    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }


    public BigDecimal getSenderBalance() {
        return senderBalance;
    }

    public void setSenderBalance(BigDecimal senderBalance) {
        this.senderBalance = senderBalance;
    }


    public BigDecimal getReceiverBalance() {
        return receiverBalance;
    }

    public void setReceiverBalance(BigDecimal receiverBalance) {
        this.receiverBalance = receiverBalance;
    }
}