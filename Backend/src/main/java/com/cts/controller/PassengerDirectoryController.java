package com.cts.controller;

import com.cts.dto.MessageResponse;
import com.cts.dto.PassengerProfileDTO;
import com.cts.security.SecurityUtil;
import com.cts.service.PassengerDirectoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/passengers")
@RequiredArgsConstructor
@Tag(name = "03. Passenger Directory")
@PreAuthorize("hasRole('CUSTOMER')")
public class PassengerDirectoryController {

    private final PassengerDirectoryService directoryService;

    @PostMapping
    @Operation(summary = "Saves a passenger profile to the authenticated customer's directory")
    public ResponseEntity<MessageResponse> savePassengerProfile(@Valid @RequestBody PassengerProfileDTO dto) {
        directoryService.savePassengerProfile(SecurityUtil.getCurrentUserId(), dto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(MessageResponse.of("PASSENGER_ADDED", "Passenger profile saved successfully."));
    }

    @PatchMapping("/{profileId}")
    @Operation(summary = "Partially updates an existing passenger profile (own profile only)")
    public ResponseEntity<MessageResponse> updatePassengerProfile(@PathVariable Long profileId,
                                                                  @RequestBody PassengerProfileDTO dto) {
        directoryService.updatePassengerProfile(profileId, dto);
        return ResponseEntity.ok(MessageResponse.of("PASSENGER_UPDATED", "Passenger profile updated successfully."));
    }

    @DeleteMapping("/{profileId}")
    @Operation(summary = "Deletes a passenger profile from the directory (own profile only)")
    public ResponseEntity<MessageResponse> removePassengerProfile(@PathVariable Long profileId) {
        directoryService.removePassengerProfile(profileId);
        return ResponseEntity.ok(MessageResponse.of("PASSENGER_REMOVED", "Passenger profile removed successfully."));
    }

    @GetMapping
    @Operation(summary = "Retrieves all saved passenger profiles for the authenticated customer")
    public ResponseEntity<List<PassengerProfileDTO>> getCustomerDirectoryPool() {
        return ResponseEntity.ok(directoryService.getCustomerDirectoryPool(SecurityUtil.getCurrentUserId()));
    }
    
    @GetMapping("/customer/{customerId}")
    @PreAuthorize("hasAnyRole('TRAVEL_AGENT', 'ADMIN')")
    @Operation(summary = "Retrieves passenger profiles for a specific customer ID")
    public ResponseEntity<List<PassengerProfileDTO>> getPassengersByCustomerId(@PathVariable Long customerId) {
        return ResponseEntity.ok(directoryService.getCustomerDirectoryPool(customerId));
    }
}
