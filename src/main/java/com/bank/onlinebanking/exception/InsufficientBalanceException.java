package com.bank.onlinebanking.exception;

/**
 * Thrown when an account does not have enough balance to perform an
 * operation. Maps to HTTP 400 BAD REQUEST.
 */
public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
