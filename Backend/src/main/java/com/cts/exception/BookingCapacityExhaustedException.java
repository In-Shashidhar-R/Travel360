package com.cts.exception;


public class BookingCapacityExhaustedException extends RuntimeException {
    private static final long serialVersionUID = 1L;

	public BookingCapacityExhaustedException(String message) {
        super(message);
    }
}