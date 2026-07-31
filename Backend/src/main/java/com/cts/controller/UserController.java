package com.cts.controller;

import com.cts.dto.*;
import com.cts.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.cts.util.AppConstants;
import com.cts.util.PaginationUtil;

import java.security.Principal;

import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
@Tag(name = "01. User Identity & Access")
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    @PreAuthorize("isAnonymous()")
    @Operation(summary = "Registers a new customer account profile")
    public ResponseEntity<UserResponseDTO> registerCustomer(@Valid @RequestBody UserRequestDTO request) {
        return new ResponseEntity<>(userService.registerCustomer(request), HttpStatus.CREATED);
    }

    @PostMapping("/agent")
    @PreAuthorize("hasRole('ADMIN')") 
    @Operation(summary = "Provisions a specialized Travel Agent into the enterprise structure")
    public ResponseEntity<UserResponseDTO> addTravelAgent(
            @Valid @RequestBody AgentRequestDTO request, 
            @RequestParam Long adminId) {
        return new ResponseEntity<>(userService.addTravelAgent(request, adminId), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @PreAuthorize("isAnonymous()")
    @Operation(summary = "Authenticates user login access tokens")
    public ResponseEntity<LoginResponseDTO> loginUser(@Valid @RequestBody LoginRequestDTO request) {
        return ResponseEntity.ok(userService.authenticateUser(request));
    }

    @PostMapping("/reset-password")
    @PreAuthorize("isAnonymous()")
    @Operation(summary = "Overwrites historical credentials with new replacement strings")
    public ResponseEntity<String> resetPassword(@Valid @RequestBody ResetPasswordDTO request) {
        return ResponseEntity.ok(userService.finalizePasswordReset(request));
    }
    
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Fetches a paginated directory of system users, optionally filtered by role")
    public ResponseEntity<PageResponse<UserResponseDTO>> getAllUsers(
            @RequestParam(required = false) String roleFilter,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "" + AppConstants.DEFAULT_PAGE_SIZE) int size,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        Pageable pageable = PaginationUtil.buildPageable(page, size, sortBy, direction);
        return ResponseEntity.ok(userService.getAllUsers(roleFilter, pageable));
    }

    @GetMapping("/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'CUSTOMER','FINANCE_OFFICER', 'COMPLIANCE_OFFICER', 'PARTNER', 'TRAVEL_AGENT')") 
    @Operation(summary = "Retrieves an individual user account profile by ID")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long userId) {
        return ResponseEntity.ok(userService.getUserById(userId));
    }
    
    @PutMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Updates a user profile structure matching explicit RBAC permissions")
    public ResponseEntity<UserResponseDTO> updateUserProfile(
            @PathVariable Long userId,
            @Valid @RequestBody UpdateUserRequestDTO request,
            Principal principal) {
        return ResponseEntity.ok(userService.updateUserProfile(userId, request, principal.getName()));
    }

    @PutMapping("/change-password")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Challenges structural credentials to rotate account password sequences safely")
    public ResponseEntity<String> changePassword(
            @Valid @RequestBody ChangePasswordRequestDTO request,
            Principal principal) {
        return ResponseEntity.ok(userService.changePassword(request, principal.getName()));
    }
}