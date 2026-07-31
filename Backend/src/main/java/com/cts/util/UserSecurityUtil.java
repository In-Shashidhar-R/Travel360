package com.cts.util;

import com.cts.entity.PassengerProfile;
import com.cts.entity.User;
import com.cts.enumeration.Role;
import com.cts.exception.DataIsolationViolationException;
import com.cts.exception.IdentityConflictException;
import com.cts.exception.InvalidCredentialsException;
import com.cts.exception.InvalidTimelineException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.PassengerProfileRepository;
import com.cts.repository.UserRepository;
import com.cts.service.AuditLogWorker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserSecurityUtil {

    private final UserRepository userRepository;
    private final PassengerProfileRepository profileRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogWorker auditLogWorker;

    private static final String PASSWORD_REGEX = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[@$!%*#?&.]).{5,}$";

    // --- Lookups ---

    public User fetchUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User account not found with ID: " + id));
    }

    public List<PassengerProfile> fetchOptionalProfiles(List<Long> ids, Long customerId) {
        return (ids == null || ids.isEmpty()) ? List.of() : fetchAndValidateProfiles(ids, customerId);
    }

    public User fetchUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("No account found for email: " + email));
    }

    public User fetchAdminUser(Long adminId) {
        User admin = fetchUser(adminId);
        if (admin.getRole() != Role.ADMIN) {
            throw new AccessDeniedException("Operation requires an administrator account.");
        }
        return admin;
    }

    // --- Credential verification ---

    public User authenticateAndVerify(String email, String rawPassword) {
        java.util.Optional<User> found = userRepository.findByEmail(email);
        if (found.isEmpty()) {
            auditLogWorker.logAsyncAction(
                    null, "USER_LOGIN_FAILED", "USER", null,
                    String.format("Failed login: no account exists for email '%s'", email),
                    com.cts.enumeration.EventLevel.ERROR);
            log.warn("Failed login attempt — unknown email: {}", email);
            throw new InvalidCredentialsException("Invalid email or password.");
        }
        User user = found.get();
        if (!passwordEncoder.matches(rawPassword, user.getPassword())) {
            auditLogWorker.logAsyncAction(
                    user, "USER_LOGIN_FAILED", "USER", user.getUserId(),
                    String.format("Failed login: incorrect password for email '%s'", email),
                    com.cts.enumeration.EventLevel.ERROR);
            log.warn("Failed login attempt for email: {}", email);
            throw new InvalidCredentialsException("Invalid email or password.");
        }
        return user;
    }

    public List<PassengerProfile> fetchAndValidateProfiles(List<Long> ids, Long customerId) {
        if (ids == null || ids.isEmpty()) {
            throw new InvalidTimelineException("Passenger profile list cannot be empty.");
        }
        List<PassengerProfile> profiles = profileRepository.findAllById(ids);
        if (profiles.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more passenger profile IDs were not found.");
        }
        for (PassengerProfile p : profiles) {
            if (!p.getCustomer().getUserId().equals(customerId)) {
                throw new DataIsolationViolationException(
                        "Passenger profile " + p.getProfileId() + " does not belong to this customer.");
            }
        }
        return profiles;
    }
    
 // --- Access Evaluation Rules for Profile Updates ---

    public void verifyProfileUpdateAuthorization(User currentUser, User targetUser) {
        boolean isSelfModification = currentUser.getUserId().equals(targetUser.getUserId());
        
        boolean isAdminAlteringAuthorizedTarget = currentUser.getRole() == Role.ADMIN && 
                (targetUser.getRole() == Role.PARTNER || targetUser.getRole() == Role.TRAVEL_AGENT);

        if (!isSelfModification && !isAdminAlteringAuthorizedTarget) {
            auditLogWorker.logAsyncAction(
                    currentUser, "UNAUTHORIZED_PROFILE_UPDATE_ATTEMPT", "USER", targetUser.getUserId(),
                    String.format("User %s denied write access to modify target profile: %s", 
                            currentUser.getEmail(), targetUser.getEmail()),
                    com.cts.enumeration.EventLevel.WARNING);
            
            throw new org.springframework.security.access.AccessDeniedException(
                    "Access Denied: You possess insufficient clearance scopes to mutate this structural target profile.");
        }
    }

    // --- Secured Password Verification Mutation Sequence ---

    public void verifyAndRotatePassword(User user, String currentRawPassword, String newRawPassword) {
        // Step 1: Prove Ownership via Password Matching Check
        if (!passwordEncoder.matches(currentRawPassword, user.getPassword())) {
            auditLogWorker.logAsyncAction(
                    user, "PASSWORD_CHANGE_VERIFICATION_FAILED", "USER", user.getUserId(),
                    "Failed password rotation request: provided current credential context was invalid.",
                    com.cts.enumeration.EventLevel.ERROR);
            
            throw new com.cts.exception.InvalidCredentialsException("The existing entry password credentials provided are incorrect.");
        }

        // Step 2: Delegate to current validation/encoding routines
        updateUserPassword(user, newRawPassword);
        
        auditLogWorker.logAsyncAction(
                user, "USER_PASSWORD_ROTATED_SUCCESSFULLY", "USER", user.getUserId(),
                "Account clearance credentials rotated and synchronized securely.");
    }

    // --- Profile Update Logger Hook ---

    public void logProfileUpdateCompleted(User operator, User target) {
        auditLogWorker.logAsyncAction(
                operator, "USER_PROFILE_UPDATED", "USER", target.getUserId(),
                String.format("Profile belonging to user ID %d updated by execution identity: %s", 
                        target.getUserId(), operator.getEmail()));
    }

    // --- Validators ---

    public void validateNewUserEmailAndSecurity(String email, String rawPassword) {
        if (rawPassword == null || !rawPassword.matches(PASSWORD_REGEX)) {
            throw new InvalidTimelineException(
                    "Password must be at least 5 characters and include a letter, a digit and a special character.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IdentityConflictException("An account with this email already exists.");
        }
    }
    
    private void validateAge(LocalDate dob) {
        if (dob == null) {
            throw new InvalidTimelineException("Date of birth is required.");
        }

        if (dob.plusYears(18).isAfter(LocalDate.now())) {
            throw new InvalidTimelineException("User must be at least 18 years old.");
        }
    }



    // --- Mutators ---

    public User commitUser(User user) {
        validateAge(user.getDateOfBirth());
        return userRepository.save(user);
    }

    public void updateUserPassword(User user, String newRawPassword) {
        if (newRawPassword == null || !newRawPassword.matches(PASSWORD_REGEX)) {
            throw new InvalidTimelineException(
                    "Password must be at least 5 characters and include a letter, a digit and a special character.");
        }
        user.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(user);
    }

    // --- Async audit loggers ---

    public void logUserLoginSuccess(User user) {
        auditLogWorker.logAsyncAction(user, "USER_LOGIN_SUCCESSFUL", "USER", user.getUserId(),
                String.format("Role %s authenticated successfully.", user.getRole().name()));
    }

    public void logCustomerRegistration(User customer) {
        auditLogWorker.logAsyncAction(customer, "ACCOUNT_CREATION_CUSTOMER", "USER", customer.getUserId(),
                String.format("Customer account created for email: %s", customer.getEmail()));
    }

    public void logAgentProvision(User admin, User agent) {
        auditLogWorker.logAsyncAction(admin, "ADMIN_PROVISIONED_TRAVEL_AGENT", "USER", agent.getUserId(),
                String.format("Admin %d provisioned travel agent for email: %s", admin.getUserId(), agent.getEmail()));
    }

    public void logPasswordRecoveryRequest(User user) {
        auditLogWorker.logAsyncAction(user, "PASSWORD_RECOVERY_REQUESTED", "USER", user.getUserId(),
                "Password recovery challenge initiated.");
    }

    public void logPasswordResetComplete(User user) {
        auditLogWorker.logAsyncAction(user, "PASSWORD_RESET_COMPLETED", "USER", user.getUserId(),
                "Account password reset successfully.");
    }

    public void logProfileLookup(User user) {
        auditLogWorker.logAsyncAction(user, "USER_PROFILE_LOOKUP_BY_ID", "USER", user.getUserId(),
                String.format("User profile %d was retrieved.", user.getUserId()));
    }
}
