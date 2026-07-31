package com.cts.exception;

import com.cts.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest req) {
        log.warn("Resource not found: {}", ex.getMessage());
        return build(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", ex.getMessage(), req);
    }

    @ExceptionHandler(BookingCapacityExhaustedException.class)
    public ResponseEntity<ErrorResponse> handleCapacity(BookingCapacityExhaustedException ex, HttpServletRequest req) {
        log.warn("Capacity exhausted: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "CAPACITY_EXHAUSTED", ex.getMessage(), req);
    }

    @ExceptionHandler(IdentityConflictException.class)
    public ResponseEntity<ErrorResponse> handleIdentityConflict(IdentityConflictException ex, HttpServletRequest req) {
        log.warn("Identity conflict: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "IDENTITY_ALREADY_EXISTS", ex.getMessage(), req);
    }

    @ExceptionHandler(DataIsolationViolationException.class)
    public ResponseEntity<ErrorResponse> handleIsolation(DataIsolationViolationException ex, HttpServletRequest req) {
        log.warn("Data isolation violation: {}", ex.getMessage());
        return build(HttpStatus.FORBIDDEN, "DATA_ISOLATION_VIOLATION", ex.getMessage(), req);
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(InvalidCredentialsException ex, HttpServletRequest req) {
        log.warn("Authentication failure: {}", ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "INVALID_CREDENTIALS", ex.getMessage(), req);
    }

    @ExceptionHandler(InventoryTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(InventoryTypeMismatchException ex, HttpServletRequest req) {
        log.warn("Inventory type mismatch: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "INVENTORY_TYPE_MISMATCH", ex.getMessage(), req);
    }

    @ExceptionHandler(InventoryInUseException.class)
    public ResponseEntity<ErrorResponse> handleInventoryInUse(InventoryInUseException ex, HttpServletRequest req) {
        log.warn("Inventory in use: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "INVENTORY_IN_USE", ex.getMessage(), req);
    }

    @ExceptionHandler(InventoryUnavailableException.class)
    public ResponseEntity<ErrorResponse> handleInventoryUnavailable(InventoryUnavailableException ex, HttpServletRequest req) {
        log.warn("Inventory unavailable for booking: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, "INVENTORY_UNAVAILABLE", ex.getMessage(), req);
    }

    @ExceptionHandler({InvalidTimelineException.class, IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<ErrorResponse> handleBadState(RuntimeException ex, HttpServletRequest req) {
        log.warn("Invalid request state: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "INVALID_TRANSACTION_STATE", ex.getMessage(), req);
    }

    // --- Spring Security ---

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest req) {
        log.warn("Access denied on {}: {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.FORBIDDEN, "ACCESS_DENIED",
                "You do not have permission to perform this operation.", req);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest req) {
        log.warn("Authentication error: {}", ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED",
                "Authentication is required or the supplied token is invalid.", req);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest req) {
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getFieldErrors()
                .forEach(error -> fieldErrors.put(error.getField(), error.getDefaultMessage()));
        log.warn("Validation failed on {}: {}", req.getRequestURI(), fieldErrors);

        ErrorResponse body = baseBuilder(HttpStatus.BAD_REQUEST, "INVALID_INPUT_PAYLOAD",
                "One or more fields failed validation.", req)
                .fieldErrors(fieldErrors)
                .build();
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex, HttpServletRequest req) {
        log.warn("Constraint violation on {}: {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, "CONSTRAINT_VIOLATION", ex.getMessage(), req);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatchParam(MethodArgumentTypeMismatchException ex, HttpServletRequest req) {
        String message = String.format("Parameter '%s' has an invalid value: '%s'.", ex.getName(), ex.getValue());
        log.warn("Argument type mismatch on {}: {}", req.getRequestURI(), message);
        return build(HttpStatus.BAD_REQUEST, "ARGUMENT_TYPE_MISMATCH", message, req);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest req) {
        String message = String.format("Required parameter '%s' is missing.", ex.getParameterName());
        log.warn("Missing parameter on {}: {}", req.getRequestURI(), message);
        return build(HttpStatus.BAD_REQUEST, "MISSING_PARAMETER", message, req);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex, HttpServletRequest req) {
        log.warn("Malformed request body on {}: {}", req.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.BAD_REQUEST, "MALFORMED_REQUEST_BODY",
                "Request body is missing, malformed, or contains an invalid value.", req);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpServletRequest req) {
        log.warn("Method not supported on {}: {}", req.getRequestURI(), ex.getMessage());
        return build(HttpStatus.METHOD_NOT_ALLOWED, "METHOD_NOT_ALLOWED", ex.getMessage(), req);
    }

    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandler(NoHandlerFoundException ex, HttpServletRequest req) {
        log.warn("No handler for {} {}", ex.getHttpMethod(), ex.getRequestURL());
        return build(HttpStatus.NOT_FOUND, "ENDPOINT_NOT_FOUND",
                "No endpoint exists for " + ex.getHttpMethod() + " " + ex.getRequestURL(), req);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest req) {
        log.warn("Data integrity violation on {}: {}", req.getRequestURI(), ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.CONFLICT, "DATA_INTEGRITY_VIOLATION",
                "The operation conflicts with an existing record or a database constraint.", req);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex, HttpServletRequest req) {
        log.error("Unhandled exception on {}", req.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_SERVER_ERROR",
                "An unexpected error occurred. Please try again later.", req);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String code, String message, HttpServletRequest req) {
        return new ResponseEntity<>(baseBuilder(status, code, message, req).build(), status);
    }

    private ErrorResponse.ErrorResponseBuilder baseBuilder(HttpStatus status, String code, String message, HttpServletRequest req) {
       
        return ErrorResponse.builder()
                .code(code)
                .message(message);
    }
}
