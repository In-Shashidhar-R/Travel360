package com.cts.exception;

public class IdentityConflictException extends RuntimeException {
    public IdentityConflictException(String message) {
        super(message);
    }
}