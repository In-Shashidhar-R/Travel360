package com.cts.exception;


public class InventoryInUseException extends RuntimeException {
    public InventoryInUseException(String message) {
        super(message);
    }
}
