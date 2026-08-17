package com.bank.onlinebanking.exception;

/**
 * Thrown when a unique resource already exists (e.g. email already
 * registered). Maps to HTTP 409 CONFLICT.
 */
public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }
}
