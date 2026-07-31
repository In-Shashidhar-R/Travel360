package com.cts.exception;

public class DataIsolationViolationException extends RuntimeException {
    public DataIsolationViolationException(String message) {
        super(message);
    }
}