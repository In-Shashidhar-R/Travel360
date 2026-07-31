package com.cts.exception;

public class InvalidTimelineException extends RuntimeException {
    public InvalidTimelineException(String message) {
        super(message);
    }
}