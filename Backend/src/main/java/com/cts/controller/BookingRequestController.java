package com.cts.controller;

import com.cts.dto.*;
import com.cts.service.BookingRequestService;
import com.cts.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/booking-requests")
@RequiredArgsConstructor
@Tag(name = "05b. Booking Requests (Customer ↔ Travel Agent)")
public class BookingRequestController {

    private final BookingRequestService bookingRequestService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Customer raises a booking request to the package's assigned travel agent")
    public ResponseEntity<BookingRequestResponseDTO> create(@Valid @RequestBody BookingRequestCreateDTO req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingRequestService.createRequest(req));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "List the current customer's booking requests")
    public ResponseEntity<PageResponse<BookingRequestResponseDTO>> mine(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        Pageable pageable = PaginationUtil.buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(bookingRequestService.listMyCustomerRequests(pageable));
    }

    @GetMapping("/assigned")
    @PreAuthorize("hasAnyRole('TRAVEL_AGENT','ADMIN')")
    @Operation(summary = "List requests assigned to the current travel agent")
    public ResponseEntity<PageResponse<BookingRequestResponseDTO>> assigned(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        Pageable pageable = PaginationUtil.buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(bookingRequestService.listAssignedRequests(pageable));
    }

    @PutMapping("/{requestId}/accept")
    @PreAuthorize("hasAnyRole('TRAVEL_AGENT','ADMIN')")
    @Operation(summary = "Agent accepts the request (status → APPROVED)")
    public ResponseEntity<BookingRequestResponseDTO> accept(
            @PathVariable Long requestId,
            @RequestBody(required = false) BookingRequestDecisionDTO decision) {
        return ResponseEntity.ok(
                bookingRequestService.acceptRequest(requestId, nonNull(decision)));
    }

    @PutMapping("/{requestId}/reject")
    @PreAuthorize("hasAnyRole('TRAVEL_AGENT','ADMIN')")
    @Operation(summary = "Agent rejects the request (status → REJECTED)")
    public ResponseEntity<BookingRequestResponseDTO> reject(
            @PathVariable Long requestId,
            @RequestBody(required = false) BookingRequestDecisionDTO decision) {
        return ResponseEntity.ok(
                bookingRequestService.rejectRequest(requestId, nonNull(decision)));
    }

    @PostMapping("/{requestId}/book")
    @PreAuthorize("hasAnyRole('TRAVEL_AGENT','ADMIN')")
    @Operation(summary = "Agent places the tour booking on the customer's behalf (status → COMPLETED)")
    public ResponseEntity<BookingRequestResponseDTO> book(
            @PathVariable Long requestId,
            @Valid @RequestBody TourBookingRequestDTO bookingPayload) {
        return ResponseEntity.ok(
                bookingRequestService.completeRequestByBooking(requestId, bookingPayload));
    }

    @GetMapping("/{requestId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','TRAVEL_AGENT','ADMIN')")
    @Operation(summary = "View a single booking request (must be customer, assigned agent, or admin)")
    public ResponseEntity<BookingRequestResponseDTO> getOne(@PathVariable Long requestId) {
        return ResponseEntity.ok(bookingRequestService.getRequestById(requestId));
    }
    
    @GetMapping("/all")
    @PreAuthorize("hasRole('ADMIN')") // Restricted to ADMIN to prevent data exposure
    @Operation(summary = "View all booking requests in the system (Admin only)")
    public ResponseEntity<List<BookingRequestResponseDTO>> getall() {
        return ResponseEntity.ok(bookingRequestService.getall());
    }

    private static BookingRequestDecisionDTO nonNull(BookingRequestDecisionDTO d) {
        return (d == null) ? new BookingRequestDecisionDTO() : d;
    }
}
