package com.cts.controller;

import com.cts.security.AuthenticatedUserPrincipal;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import java.util.List;

final class ControllerTestSupport {

    private ControllerTestSupport() {
    }

    static final Validator NOOP_VALIDATOR = new Validator() {
        @Override
        public boolean supports(Class<?> clazz) {
            return false;
        }

        @Override
        public void validate(Object target, Errors errors) {
        }
    };

    static void loginAsAdmin() {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(
                1L, "admin@a.com", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    static void clear() {
        SecurityContextHolder.clearContext();
    }
}
