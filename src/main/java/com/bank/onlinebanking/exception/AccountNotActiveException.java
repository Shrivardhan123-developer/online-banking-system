package com.bank.onlinebanking.exception;

/**
 * Thrown when an operation is attempted on an account that is not in an
 * ACTIVE state. Maps to HTTP 400 BAD REQUEST.
 */
public class AccountNotActiveException extends RuntimeException {

    public AccountNotActiveException(String message) {
        super(message);
    }
}
