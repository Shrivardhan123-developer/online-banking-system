package com.bank.onlinebanking.exception;

/**
 * Thrown when an authenticated user attempts an operation they are not
 * permitted to perform (e.g. accessing another customer's data).
 * Maps to HTTP 403 FORBIDDEN.
 */
public class UnauthorizedOperationException extends RuntimeException {

    public UnauthorizedOperationException(String message) {
        super(message);
    }
}
