package com.cts.controller;

import com.cts.dto.*;
import com.cts.enumeration.InventoryType;
import com.cts.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1/inventories")
@RequiredArgsConstructor
public class InventoryController {

    private static final String TAG_FLIGHT = "04a. Inventory - Flight";
    private static final String TAG_HOTEL = "04b. Inventory - Hotel";
    private static final String TAG_BUS = "04c. Inventory - Bus";
    private static final String TAG_CAB = "04d. Inventory - Cab";
    private static final String TAG_TOUR = "04e. Inventory - Tour Package";
    private static final String TAG_BROWSE = "04f. Inventory - Browse & Lifecycle";

    private final InventoryService inventoryService;
    
    @PostMapping("/flight")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Provision a new flight inventory", tags = {TAG_FLIGHT})
    public ResponseEntity<FlightInventoryResponseDTO> provisionFlight(@Valid @RequestBody FlightInventoryRequestDTO request) {
        return new ResponseEntity<>(inventoryService.provisionFlight(request), HttpStatus.CREATED);
    }

    @PutMapping("/flight/{inventoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing flight inventory", tags = {TAG_FLIGHT})
    public ResponseEntity<FlightInventoryResponseDTO> updateFlight(
            @PathVariable Long inventoryId, @Valid @RequestBody FlightInventoryRequestDTO request) {
        return ResponseEntity.ok(inventoryService.updateFlight(inventoryId, request));
    }

    @DeleteMapping("/flight/{inventoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a flight inventory (only if no bookings reference it)", tags = {TAG_FLIGHT})
    public ResponseEntity<String> deleteFlight(@PathVariable Long inventoryId) {
        inventoryService.deleteInventory(inventoryId, InventoryType.FLIGHT);
        return ResponseEntity.ok("Flight inventory " + inventoryId + " deleted successfully.");
    }

    // HOTEL BOOKING

    @PostMapping("/hotel")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Provision a new hotel inventory", tags = {TAG_HOTEL})
    public ResponseEntity<HotelInventoryResponseDTO> provisionHotel(@Valid @RequestBody HotelInventoryRequestDTO request) {
        return new ResponseEntity<>(inventoryService.provisionHotel(request), HttpStatus.CREATED);
    }

    @PutMapping("/hotel/{inventoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing hotel inventory", tags = {TAG_HOTEL})
    public ResponseEntity<HotelInventoryResponseDTO> updateHotel(
            @PathVariable Long inventoryId, @Valid @RequestBody HotelInventoryRequestDTO request) {
        return ResponseEntity.ok(inventoryService.updateHotel(inventoryId, request));
    }

    @DeleteMapping("/hotel/{inventoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a hotel inventory (only if no bookings reference it)", tags = {TAG_HOTEL})
    public ResponseEntity<String> deleteHotel(@PathVariable Long inventoryId) {
        inventoryService.deleteInventory(inventoryId, InventoryType.HOTEL);
        return ResponseEntity.ok("Hotel inventory " + inventoryId + " deleted successfully.");
    }

    // BUS BOOKING

    @PostMapping("/bus")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Provision a new bus inventory", tags = {TAG_BUS})
    public ResponseEntity<BusInventoryResponseDTO> provisionBus(@Valid @RequestBody BusInventoryRequestDTO request) {
        return new ResponseEntity<>(inventoryService.provisionBus(request), HttpStatus.CREATED);
    }

    @PutMapping("/bus/{inventoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing bus inventory", tags = {TAG_BUS})
    public ResponseEntity<BusInventoryResponseDTO> updateBus(
            @PathVariable Long inventoryId, @Valid @RequestBody BusInventoryRequestDTO request) {
        return ResponseEntity.ok(inventoryService.updateBus(inventoryId, request));
    }

    @DeleteMapping("/bus/{inventoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a bus inventory (only if no bookings reference it)", tags = {TAG_BUS})
    public ResponseEntity<String> deleteBus(@PathVariable Long inventoryId) {
        inventoryService.deleteInventory(inventoryId, InventoryType.BUS);
        return ResponseEntity.ok("Bus inventory " + inventoryId + " deleted successfully.");
    }

    // CAB BOOKING

    @PostMapping("/cab")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Provision a new cab inventory", tags = {TAG_CAB})
    public ResponseEntity<CabInventoryResponseDTO> provisionCab(@Valid @RequestBody CabInventoryRequestDTO request) {
        return new ResponseEntity<>(inventoryService.provisionCab(request), HttpStatus.CREATED);
    }

    @PutMapping("/cab/{inventoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing cab inventory", tags = {TAG_CAB})
    public ResponseEntity<CabInventoryResponseDTO> updateCab(
            @PathVariable Long inventoryId, @Valid @RequestBody CabInventoryRequestDTO request) {
        return ResponseEntity.ok(inventoryService.updateCab(inventoryId, request));
    }

    @DeleteMapping("/cab/{inventoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a cab inventory (only if no bookings reference it)", tags = {TAG_CAB})
    public ResponseEntity<String> deleteCab(@PathVariable Long inventoryId) {
        inventoryService.deleteInventory(inventoryId, InventoryType.CAB);
        return ResponseEntity.ok("Cab inventory " + inventoryId + " deleted successfully.");
    }

    // TOUR PACKAGE BOOKING

    @PostMapping("/tour-package")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Provision a new tour package inventory", tags = {TAG_TOUR})
    public ResponseEntity<TourInventoryResponseDTO> provisionTour(@Valid @RequestBody TourInventoryRequestDTO request) {
        return new ResponseEntity<>(inventoryService.provisionTour(request), HttpStatus.CREATED);
    }

    @PutMapping("/tour-package/{inventoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing tour package inventory", tags = {TAG_TOUR})
    public ResponseEntity<TourInventoryResponseDTO> updateTour(
            @PathVariable Long inventoryId, @Valid @RequestBody TourInventoryRequestDTO request) {
        return ResponseEntity.ok(inventoryService.updateTour(inventoryId, request));
    }

    @DeleteMapping("/tour-package/{inventoryId}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete a tour package inventory (only if no bookings reference it)", tags = {TAG_TOUR})
    public ResponseEntity<String> deleteTour(@PathVariable Long inventoryId) {
        inventoryService.deleteInventory(inventoryId, InventoryType.TOUR_PACKAGE);
        return ResponseEntity.ok("Tour package inventory " + inventoryId + " deleted successfully.");
    }

    // INVENTORIES

    @GetMapping("/{inventoryId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "Fetch a single inventory item by ID with live dynamic pricing applied", tags = {TAG_BROWSE})
    public ResponseEntity<Object> getInventoryById(
            @PathVariable Long inventoryId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {
        return ResponseEntity.ok(inventoryService.getInventoryById(inventoryId, targetDate));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "Search active inventories by route source and destination", tags = {TAG_BROWSE})
    public ResponseEntity<List<?>> searchInventories(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {
        return ResponseEntity.ok(inventoryService.searchByRoute(source, destination, targetDate));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "List all inventories with live dynamic pricing applied", tags = {TAG_BROWSE})
    public ResponseEntity<List<?>> getAllInventories(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {
        return ResponseEntity.ok(inventoryService.getAllInventories(targetDate));
    }

    @GetMapping("/filter")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "Filter active inventories by an advanced set of optional criteria", tags = {TAG_BROWSE})
    public ResponseEntity<List<?>> filterInventories(
            @RequestParam(required = false) InventoryType itemType,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String district,
            @RequestParam(required = false) String city,
            @RequestParam(required = false) String source,
            @RequestParam(required = false) String destination,
            @RequestParam(required = false) Integer requiredCapacity,
            @RequestParam(required = false) Integer durationDays,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate targetDate) {

        List<?> filteredResults = inventoryService.filterInventories(
                itemType, state, district, city, source, destination, requiredCapacity, durationDays, maxPrice, targetDate);
        return ResponseEntity.ok(filteredResults);
    }

    @PutMapping("/{inventoryId}/deactivate")
    @PreAuthorize("hasAnyRole('ADMIN','PARTNER')")
    @Operation(summary = "Deactivate an inventory item (admins for any; partners for inventories they own)", tags = {TAG_BROWSE})
    public ResponseEntity<Object> deactivateInventory(@PathVariable Long inventoryId) {
        return ResponseEntity.ok(inventoryService.deactivateInventory(inventoryId));
    }

    @PutMapping("/{inventoryId}/activate")
    @PreAuthorize("hasAnyRole('ADMIN','PARTNER')")
    @Operation(summary = "Reactivate an inventory item (admins for any; partners for inventories they own)", tags = {TAG_BROWSE})
    public ResponseEntity<Object> activateInventory(@PathVariable Long inventoryId) {
        return ResponseEntity.ok(inventoryService.activateInventory(inventoryId));
    }

    @GetMapping("/mine")
    @PreAuthorize("hasRole('PARTNER')")
    @Operation(summary = "List inventories owned by the currently-authenticated partner", tags = {TAG_BROWSE})
    public ResponseEntity<com.cts.dto.PageResponse<Object>> getMyInventories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String sortBy,
            @RequestParam(defaultValue = "DESC") String direction) {
        org.springframework.data.domain.Pageable pageable =
                com.cts.util.PaginationUtil.buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(inventoryService.getMyInventories(pageable));
    }
}
