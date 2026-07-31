package com.cts.exception;

import com.cts.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

class GlobalExceptionHandlerTest {

    private GlobalExceptionHandler handler;
    private HttpServletRequest request;

    @BeforeEach
    void setUp() {
        handler = new GlobalExceptionHandler();
        request = mock(HttpServletRequest.class);
        lenient().when(request.getRequestURI()).thenReturn("/api/v1/test");
    }

    @Test
    void resourceNotFound_returns404() {
        ResponseEntity<ErrorResponse> response =
                handler.handleNotFound(new ResourceNotFoundException("missing"), request);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("RESOURCE_NOT_FOUND", response.getBody().getCode());
        assertEquals("missing", response.getBody().getMessage());
    }

    @Test
    void capacityExhausted_returns409() {
        ResponseEntity<ErrorResponse> response =
                handler.handleCapacity(new BookingCapacityExhaustedException("full"), request);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("CAPACITY_EXHAUSTED", response.getBody().getCode());
    }

    @Test
    void dataIsolation_returns403() {
        ResponseEntity<ErrorResponse> response =
                handler.handleIsolation(new DataIsolationViolationException("not yours"), request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("DATA_ISOLATION_VIOLATION", response.getBody().getCode());
    }

    @Test
    void invalidCredentials_returns401() {
        ResponseEntity<ErrorResponse> response =
                handler.handleInvalidCredentials(new InvalidCredentialsException("bad"), request);
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("INVALID_CREDENTIALS", response.getBody().getCode());
    }

    @Test
    void typeMismatch_returns400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleTypeMismatch(new InventoryTypeMismatchException("wrong type"), request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("INVENTORY_TYPE_MISMATCH", response.getBody().getCode());
    }

    @Test
    void badState_returns400() {
        ResponseEntity<ErrorResponse> response =
                handler.handleBadState(new IllegalArgumentException("bad arg"), request);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("INVALID_TRANSACTION_STATE", response.getBody().getCode());
    }

    @Test
    void accessDenied_returns403() {
        ResponseEntity<ErrorResponse> response =
                handler.handleAccessDenied(new AccessDeniedException("denied"), request);
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("ACCESS_DENIED", response.getBody().getCode());
    }

    @Test
    void unexpected_returns500() {
        ResponseEntity<ErrorResponse> response =
                handler.handleUnexpected(new RuntimeException("boom"), request);
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("INTERNAL_SERVER_ERROR", response.getBody().getCode());
    }
}
