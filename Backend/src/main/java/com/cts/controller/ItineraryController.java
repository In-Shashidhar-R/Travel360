package com.cts.controller;

import com.cts.dto.ItineraryEntryDTO;
import com.cts.service.ItineraryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/v1/itineraries")
@RequiredArgsConstructor
@Tag(name = "05c. Itinerary Management")
public class ItineraryController {

    private final ItineraryService itineraryService;

    @GetMapping("/upcoming")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Upcoming trips for the current customer (travelDate >= today)")
    public ResponseEntity<List<ItineraryEntryDTO>> upcoming() {
        return ResponseEntity.ok(itineraryService.getMyUpcomingTrips());
    }

    @GetMapping("/past")
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Past trips for the current customer (travelDate < today)")
    public ResponseEntity<List<ItineraryEntryDTO>> past() {
        return ResponseEntity.ok(itineraryService.getMyPastTrips());
    }

    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('ADMIN','TRAVEL_AGENT','CORPORATE_TRAVEL_MANAGER')")
    @Operation(summary = "All trips for any customer (admin / agent / corporate-manager view)")
    public ResponseEntity<List<ItineraryEntryDTO>> forCustomer(@PathVariable Long customerId) {
        return ResponseEntity.ok(itineraryService.getTripsForCustomer(customerId));
    }
}
