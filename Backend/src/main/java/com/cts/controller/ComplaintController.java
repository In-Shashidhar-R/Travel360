package com.cts.controller;

import com.cts.dto.ComplaintCreateDTO;
import com.cts.dto.ComplaintResolveDTO;
import com.cts.dto.ComplaintResponseDTO;
import com.cts.dto.PageResponse;
import com.cts.enumeration.ComplaintStatus;
import com.cts.service.ComplaintService;
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
@RequestMapping("/api/v1/complaints")
@RequiredArgsConstructor
@Tag(name = "10. Complaints Management")
public class ComplaintController {

    private final ComplaintService complaintService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    @Operation(summary = "Customer raises a new complaint (optionally tied to a booking)")
    public ResponseEntity<ComplaintResponseDTO> raiseComplaint(@Valid @RequestBody ComplaintCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(complaintService.raiseComplaint(request));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE_OFFICER','CUSTOMER')")
    @Operation(summary = "List complaints with optional dynamic filters (Customers are isolated to their own records)")
    public ResponseEntity<PageResponse<ComplaintResponseDTO>> getComplaints(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) ComplaintStatus status,
            @RequestParam(required = false) Long bookingId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        
        Pageable pageable = PaginationUtil.buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(complaintService.getComplaints(userId, status, bookingId, pageable));
    }

    @GetMapping("/{complaintId}")
    @PreAuthorize("hasAnyRole('CUSTOMER','COMPLIANCE_OFFICER','ADMIN')")
    @Operation(summary = "View a complaint (own complaint, or any for compliance officer / admin)")
    public ResponseEntity<ComplaintResponseDTO> getComplaint(@PathVariable Long complaintId) {
        return ResponseEntity.ok(complaintService.getComplaintById(complaintId));
    }

    @PatchMapping("/{complaintId}/in-progress")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE_OFFICER')")
    @Operation(summary = "Mark a complaint as IN_PROGRESS with a handling note (Partial Update)")
    public ResponseEntity<ComplaintResponseDTO> markInProgress(
            @PathVariable Long complaintId, @Valid @RequestBody ComplaintResolveDTO request) {
        return ResponseEntity.ok(complaintService.markInProgress(complaintId, request));
    }

    @PatchMapping("/{complaintId}/resolve")
    @PreAuthorize("hasAnyRole('ADMIN','COMPLIANCE_OFFICER')")
    @Operation(summary = "Resolve a complaint with a resolution note (Partial Update)")
    public ResponseEntity<ComplaintResponseDTO> resolveComplaint(
            @PathVariable Long complaintId, @Valid @RequestBody ComplaintResolveDTO request) {
        return ResponseEntity.ok(complaintService.resolveComplaint(complaintId, request));
    }
}