package com.cts.controller;

import com.cts.dto.*;
import com.cts.security.SecurityUtil;
import com.cts.service.BookingService;
import com.cts.util.AppConstants;
import com.cts.util.PaginationUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Tag(name = "05. Booking, Reservation & Itinerary")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping("/flight")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Executes a distinct Flight ticket purchase transaction")
    public ResponseEntity<FlightBookingResponseDTO> bookFlight(@Valid @RequestBody FlightBookingRequestDTO request) {
        request.setCustomerId(SecurityUtil.getCurrentUserId());
        return new ResponseEntity<>(bookingService.bookFlight(request), HttpStatus.CREATED);
    }

    @PostMapping("/hotel")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Executes a dynamic Hotel check-in stay booking reservation")
    public ResponseEntity<HotelBookingResponseDTO> bookHotel(@Valid @RequestBody HotelBookingRequestDTO request) {
        request.setCustomerId(SecurityUtil.getCurrentUserId());
        return new ResponseEntity<>(bookingService.bookHotel(request), HttpStatus.CREATED);
    }

    @PostMapping("/bus")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Executes a Bus seat reservation complete with terminal points mapping")
    public ResponseEntity<BusBookingResponseDTO> bookBus(@Valid @RequestBody BusBookingRequestDTO request) {
        request.setCustomerId(SecurityUtil.getCurrentUserId());
        return new ResponseEntity<>(bookingService.bookBus(request), HttpStatus.CREATED);
    }

    @PostMapping("/cab")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Executes a local point-to-point Cab vehicle rental request")
    public ResponseEntity<CabBookingResponseDTO> bookCab(@Valid @RequestBody CabBookingRequestDTO request) {
        request.setCustomerId(SecurityUtil.getCurrentUserId());
        return new ResponseEntity<>(bookingService.bookCab(request), HttpStatus.CREATED);
    }

    @PostMapping("/tour-package")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Executes a comprehensive global vacation Tour Package reservation")
    public ResponseEntity<TourBookingResponseDTO> bookTour(@Valid @RequestBody TourBookingRequestDTO request) {
        request.setCustomerId(SecurityUtil.getCurrentUserId());
        return new ResponseEntity<>(bookingService.bookTour(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Administrative paginated lookup of every booking transaction polymorphically")
    public ResponseEntity<PageResponse<Object>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "bookingId") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        Pageable pageable = PaginationUtil.buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(bookingService.getAllBookings(pageable));
    }

    @GetMapping("/{bookingId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "Fetches a single booking by its identifier (own booking only, unless admin)")
    public ResponseEntity<Object> getBookingById(@PathVariable Long bookingId) {
        return ResponseEntity.ok(bookingService.getBookingById(bookingId));
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "Fetches paginated personal booking history (own history only, unless admin)")
    public ResponseEntity<PageResponse<Object>> getCustomerBookings(
            @PathVariable Long customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "bookingId") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        SecurityUtil.assertSelfOrAdmin(customerId);
        Pageable pageable = PaginationUtil.buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(bookingService.getCustomerBookings(customerId, pageable));
    }

    @PutMapping("/cancel/{bookingId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Processes a tiered-refund partial cancellation for selected passengers (own booking only)")
    public ResponseEntity<InvoiceCancelResponseDTO> cancelBooking(
            @PathVariable Long bookingId,
            @Valid @RequestBody PartialCancelRequestDTO request) {
        request.setCustomerId(SecurityUtil.getCurrentUserId());
        return ResponseEntity.ok(bookingService.cancelBooking(bookingId, request));
    }

    @PutMapping("/cancel-all/{bookingId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Cancels an entire booking and processes the tiered refund (own booking only)")
    public ResponseEntity<InvoiceCancelResponseDTO> cancelEntireBooking(
            @PathVariable Long bookingId,
            @RequestBody(required = false) PartialCancelRequestDTO request) {
        String remarks = (request != null) ? request.getCancellationRemarks() : null;
        return ResponseEntity.ok(
                bookingService.cancelEntireBooking(bookingId, SecurityUtil.getCurrentUserId(), remarks));
    }
}
