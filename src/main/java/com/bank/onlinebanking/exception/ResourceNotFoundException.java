package com.bank.onlinebanking.exception;

/**
 * Thrown when a requested resource (customer, account, transaction, ...)
 * cannot be found. Maps to HTTP 404 NOT FOUND.
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
