package com.cts.util;

import com.cts.entity.PassengerProfile;
import com.cts.entity.User;
import com.cts.enumeration.Role;
import com.cts.exception.*;
import com.cts.repository.PassengerProfileRepository;
import com.cts.repository.UserRepository;
import com.cts.service.AuditLogWorker;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class UserSecurityUtilTest {

    @Mock UserRepository userRepository;
    @Mock PassengerProfileRepository profileRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock AuditLogWorker auditLogWorker;
    @InjectMocks UserSecurityUtil util;

    @Test
    void fetchUser_returnsWhenFound() {
        User u = User.builder().userId(1L).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        assertSame(u, util.fetchUser(1L));
    }

    @Test
    void fetchUser_notFound_throws() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> util.fetchUser(1L));
    }
    
    @Test
    void fetchUserByEmail_returnsWhenFound() {
        User u = User.builder().email("x@x.com").build();
        when(userRepository.findByEmail("x@x.com")).thenReturn(Optional.of(u));
        assertSame(u, util.fetchUserByEmail("x@x.com"));
    }

    @Test
    void fetchUserByEmail_notFound_throws() {
        when(userRepository.findByEmail("nope@x.com")).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> util.fetchUserByEmail("nope@x.com"));
    }

    @Test
    void fetchAdminUser_admin_returnsUser() {
        User admin = User.builder().userId(1L).role(Role.ADMIN).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(admin));
        assertSame(admin, util.fetchAdminUser(1L));
    }

    @Test
    void fetchAdminUser_nonAdmin_throwsAccessDenied() {
        User u = User.builder().userId(1L).role(Role.CUSTOMER).build();
        when(userRepository.findById(1L)).thenReturn(Optional.of(u));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> util.fetchAdminUser(1L));
    }

    @Test
    void authenticateAndVerify_unknownEmail_throws() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(InvalidCredentialsException.class,
                () -> util.authenticateAndVerify("no@x.com", "pw"));
    }

    @Test
    void authenticateAndVerify_wrongPassword_throws() {
        User u = User.builder().email("e@e.com").password("HASH").build();
        when(userRepository.findByEmail("e@e.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("bad", "HASH")).thenReturn(false);
        assertThrows(InvalidCredentialsException.class,
                () -> util.authenticateAndVerify("e@e.com", "bad"));
    }

    @Test
    void authenticateAndVerify_goodPassword_returnsUser() {
        User u = User.builder().email("e@e.com").password("HASH").build();
        when(userRepository.findByEmail("e@e.com")).thenReturn(Optional.of(u));
        when(passwordEncoder.matches("good", "HASH")).thenReturn(true);
        assertSame(u, util.authenticateAndVerify("e@e.com", "good"));
    }

    @Test
    void fetchOptionalProfiles_nullOrEmpty_returnsEmptyList() {
        assertTrue(util.fetchOptionalProfiles(null, 1L).isEmpty());
        assertTrue(util.fetchOptionalProfiles(List.of(), 1L).isEmpty());
    }

    @Test
    void fetchOptionalProfiles_nonEmpty_delegatesToValidator() {
        User customer = User.builder().userId(1L).build();
        PassengerProfile p = PassengerProfile.builder().profileId(1L).customer(customer).build();
        when(profileRepository.findAllById(List.of(1L))).thenReturn(List.of(p));
        List<PassengerProfile> result = util.fetchOptionalProfiles(List.of(1L), 1L);
        assertEquals(1, result.size());
    }

    @Test
    void fetchAndValidateProfiles_empty_throws() {
        assertThrows(InvalidTimelineException.class,
                () -> util.fetchAndValidateProfiles(List.of(), 1L));
    }

    @Test
    void fetchAndValidateProfiles_missingOne_throws() {
        when(profileRepository.findAllById(List.of(1L, 2L))).thenReturn(List.of());
        assertThrows(ResourceNotFoundException.class,
                () -> util.fetchAndValidateProfiles(List.of(1L, 2L), 1L));
    }

    @Test
    void fetchAndValidateProfiles_wrongOwner_throws() {
        User other = User.builder().userId(99L).build();
        PassengerProfile p = PassengerProfile.builder().profileId(1L).customer(other).build();
        when(profileRepository.findAllById(List.of(1L))).thenReturn(List.of(p));
        assertThrows(DataIsolationViolationException.class,
                () -> util.fetchAndValidateProfiles(List.of(1L), 1L));
    }

    @Test
    void fetchAndValidateProfiles_owner_returnsList() {
        User me = User.builder().userId(1L).build();
        PassengerProfile p = PassengerProfile.builder().profileId(1L).customer(me).build();
        when(profileRepository.findAllById(List.of(1L))).thenReturn(List.of(p));
        assertEquals(1, util.fetchAndValidateProfiles(List.of(1L), 1L).size());
    }

    @Test
    void validateNewUserEmailAndSecurity_weakPassword_throws() {
        assertThrows(InvalidTimelineException.class,
                () -> util.validateNewUserEmailAndSecurity("x@x.com", "ab"));
    }

    @Test
    void validateNewUserEmailAndSecurity_duplicateEmail_throws() {
        when(userRepository.existsByEmail("x@x.com")).thenReturn(true);
        assertThrows(IdentityConflictException.class,
                () -> util.validateNewUserEmailAndSecurity("x@x.com", "Pw1!ok"));
    }

    @Test
    void validateNewUserEmailAndSecurity_ok_returnsSilently() {
        when(userRepository.existsByEmail("x@x.com")).thenReturn(false);
        assertDoesNotThrow(() -> util.validateNewUserEmailAndSecurity("x@x.com", "Pw1!ok"));
    }


    @Test
    void updateUserPassword_weak_throws() {
        User u = User.builder().build();
        assertThrows(InvalidTimelineException.class, () -> util.updateUserPassword(u, "ab"));
    }

    @Test
    void updateUserPassword_strong_hashesAndSaves() {
        User u = User.builder().build();
        when(passwordEncoder.encode("Pw1!ok")).thenReturn("HASH");
        util.updateUserPassword(u, "Pw1!ok");
        assertEquals("HASH", u.getPassword());
        verify(userRepository).save(u);
    }

    @Test
    void logHelpers_delegateToAuditWorker() {
        User u = User.builder()
                .userId(1L).name("R").email("r@r.com").role(Role.CUSTOMER).build();
        User admin = User.builder()
                .userId(2L).name("A").email("a@a.com").role(Role.ADMIN).build();

        util.logUserLoginSuccess(u);
        util.logCustomerRegistration(u);
        util.logAgentProvision(admin, u);
        util.logPasswordRecoveryRequest(u);
        util.logPasswordResetComplete(u);
        util.logProfileLookup(u);

        verify(auditLogWorker, times(6))
                .logAsyncAction(any(User.class), anyString(), anyString(), any(), anyString());
    }
}
