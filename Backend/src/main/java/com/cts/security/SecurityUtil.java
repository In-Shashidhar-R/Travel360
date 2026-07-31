package com.cts.security;

import com.cts.exception.DataIsolationViolationException;
import com.cts.exception.InvalidCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class SecurityUtil {

    public static final String ADMIN_AUTHORITY = "ROLE_ADMIN";

    private SecurityUtil() {
    }

    public static Long getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUserPrincipal principal
                && principal.getUserId() != null) {
            return principal.getUserId();
        }
        throw new InvalidCredentialsException("No authenticated user found in the security context.");
    }

    public static boolean isAdmin() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null && auth.getAuthorities().stream()
                .anyMatch(granted -> ADMIN_AUTHORITY.equals(granted.getAuthority()));
    }

    public static boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) {
            return false;
        }
        String authority = "ROLE_" + role;
        return auth.getAuthorities().stream()
                .anyMatch(granted -> authority.equals(granted.getAuthority()));
    }

    public static void assertSelfOrAdmin(Long targetUserId) {
        if (!isAdmin() && !getCurrentUserId().equals(targetUserId)) {
            throw new DataIsolationViolationException(
                    "Access denied: you may only access your own resources.");
        }
    }
}
