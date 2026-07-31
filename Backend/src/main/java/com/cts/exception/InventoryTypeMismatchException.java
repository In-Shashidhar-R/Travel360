package com.cts.exception;

public class InventoryTypeMismatchException extends RuntimeException {
    public InventoryTypeMismatchException(String message) {
        super(message);
    }
}
