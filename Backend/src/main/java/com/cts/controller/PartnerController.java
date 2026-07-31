package com.cts.controller;

import com.cts.dto.PageResponse;
import com.cts.dto.PartnerRequestDTO;
import com.cts.dto.PartnerResponseDTO;
import com.cts.dto.UpdatePartnerRequestDTO;
import com.cts.service.PartnerService;
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
@RequestMapping("/api/v1/partners")
@RequiredArgsConstructor
@Tag(name = "02. Partner Management")
public class PartnerController {

    private final PartnerService partnerService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Provisions a new travel merchant business partner profile")
    public ResponseEntity<PartnerResponseDTO> registerPartner(@Valid @RequestBody PartnerRequestDTO request) {
        return new ResponseEntity<>(partnerService.registerPartner(request), HttpStatus.CREATED);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER')")
    @Operation(summary = "Retrieves a paginated listing of all registered travel merchants")
    public ResponseEntity<PageResponse<PartnerResponseDTO>> getAllPartners(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "partnerId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PaginationUtil.buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(partnerService.getAllPartners(pageable));
    }
    
    @PutMapping("/{partnerId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Alters systemic details inside a business partner registration profile under strict RBAC restrictions")
    public ResponseEntity<PartnerResponseDTO> updatePartnerProfile(
            @PathVariable Long partnerId,
            @Valid @RequestBody UpdatePartnerRequestDTO request,
            java.security.Principal principal) {
        return ResponseEntity.ok(partnerService.updatePartnerProfile(partnerId, request, principal.getName()));
    }
}
