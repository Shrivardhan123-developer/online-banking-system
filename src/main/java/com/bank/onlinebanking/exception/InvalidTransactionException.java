package com.bank.onlinebanking.exception;

/**
 * Thrown when a transaction cannot be validated (e.g. zero or negative
 * amount, same source and destination). Maps to HTTP 400 BAD REQUEST.
 */
public class InvalidTransactionException extends RuntimeException {

    public InvalidTransactionException(String message) {
        super(message);
    }
}
