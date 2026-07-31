package com.cts.serviceimpl;

import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.enumeration.Status;
import com.cts.exception.DataIsolationViolationException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.mapper.BookingMapper;
import com.cts.mapper.PartnerMapper;
import com.cts.mapper.PassengerMapper;
import com.cts.repository.*;
import com.cts.security.AuthenticatedUserPrincipal;
import com.cts.util.CoreTransactionalUtil;
import com.cts.util.PaymentProcessingUtil;
import com.cts.util.UserSecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RemainingServiceImplTests {

    private static void loginAsAdmin() {
        AuthenticatedUserPrincipal p = new AuthenticatedUserPrincipal(
                99L, "admin@a.com", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(p, null, p.getAuthorities()));
    }

    private static final Pageable PAGE = PageRequest.of(0, 10);

    @Nested
    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    class InvoiceServiceTest {
        @Mock InvoiceRepository invoiceRepository;
        @Mock BookingMapper bookingMapper;
        @Mock UserSecurityUtil securityUtil;
        @Mock CoreTransactionalUtil transUtil;
        @InjectMocks InvoiceServiceImpl service;

        @BeforeEach void ctx() { loginAsAdmin(); }
        @AfterEach void clr() { SecurityContextHolder.clearContext(); }

        @Test
        void getAllInvoices_paginates() {
            Invoice inv = mock(Invoice.class);
            when(invoiceRepository.findAll(PAGE)).thenReturn(new PageImpl<>(List.of(inv)));
            when(bookingMapper.toInvoiceResponseDTO(inv)).thenReturn(new InvoiceResponseDTO());
            assertEquals(1, service.getAllInvoices(PAGE).getContent().size());
        }

        @Test
        void getInvoicesByCustomer_paginates() {
            User u = mock(User.class);
            Invoice inv = mock(Invoice.class);
            when(securityUtil.fetchUser(1L)).thenReturn(u);
            when(invoiceRepository.findByCustomer(u, PAGE)).thenReturn(new PageImpl<>(List.of(inv)));
            when(bookingMapper.toInvoiceResponseDTO(inv)).thenReturn(new InvoiceResponseDTO());
            assertEquals(1, service.getInvoicesByCustomer(1L, PAGE).getContent().size());
        }

        @Test
        void getInvoiceById_checksOwnerThenMaps() {
            Invoice inv = mock(Invoice.class);
            Booking b = mock(Booking.class);
            User u = mock(User.class);
            when(inv.getBooking()).thenReturn(b);
            when(b.getCustomer()).thenReturn(u);
            when(u.getUserId()).thenReturn(1L);
            InvoiceResponseDTO dto = new InvoiceResponseDTO();
            when(transUtil.fetchInvoice(5L)).thenReturn(inv);
            when(bookingMapper.toInvoiceResponseDTO(inv)).thenReturn(dto);
            assertSame(dto, service.getInvoiceById(5L));
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    class PaymentServiceTest {
        @Mock PaymentRepository paymentRepository;
        @Mock CoreTransactionalUtil transUtil;
        @Mock PaymentProcessingUtil paymentUtil;
        @InjectMocks PaymentServiceImpl service;

        @BeforeEach void ctx() { loginAsAdmin(); }
        @AfterEach void clr() { SecurityContextHolder.clearContext(); }

        @Test
        void executePayment_runsSettlement() {
            PaymentRequestDTO req = mock(PaymentRequestDTO.class);
            when(req.getInvoiceId()).thenReturn(7L);
            when(req.getMethod()).thenReturn("UPI");
            Invoice inv = mock(Invoice.class);
            when(paymentUtil.fetchUnpaidInvoice(7L)).thenReturn(inv);
            service.executePayment(req);
            verify(paymentUtil).synchronizePaymentState(inv);
            verify(paymentUtil).processFinalSettlement(inv, "UPI");
        }

        @Test
        void getAllPayments_paginates() {
            Payment p = mock(Payment.class);
            when(paymentRepository.findAll(PAGE)).thenReturn(new PageImpl<>(List.of(p)));
            when(paymentUtil.toPaymentResponseDTO(p)).thenReturn(new PaymentResponseDTO());
            assertEquals(1, service.getAllPayments(PAGE).getContent().size());
        }

        @Test
        void getPaymentById_checksOwnerThenMaps() {
            Payment p = mock(Payment.class);
            Invoice inv = mock(Invoice.class);
            Booking b = mock(Booking.class);
            User u = mock(User.class);
            when(p.getInvoice()).thenReturn(inv);
            when(inv.getBooking()).thenReturn(b);
            when(b.getCustomer()).thenReturn(u);
            when(u.getUserId()).thenReturn(1L);
            PaymentResponseDTO dto = new PaymentResponseDTO();
            when(paymentRepository.findById(3L)).thenReturn(Optional.of(p));
            when(paymentUtil.toPaymentResponseDTO(p)).thenReturn(dto);
            assertSame(dto, service.getPaymentById(3L));
        }

        @Test
        void getPaymentById_notFound_throws() {
            when(paymentRepository.findById(3L)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> service.getPaymentById(3L));
        }

        @Test
        void getPaymentsByInvoice_listsForInvoice() {
            Invoice inv = mock(Invoice.class);
            Booking b = mock(Booking.class);
            User u = mock(User.class);
            when(inv.getBooking()).thenReturn(b);
            when(b.getCustomer()).thenReturn(u);
            when(u.getUserId()).thenReturn(1L);
            Payment p = mock(Payment.class);
            when(transUtil.fetchInvoice(8L)).thenReturn(inv);
            when(paymentRepository.findByInvoice(inv)).thenReturn(List.of(p));
            when(paymentUtil.toPaymentResponseDTO(p)).thenReturn(new PaymentResponseDTO());
            assertEquals(1, service.getPaymentsByInvoice(8L).size());
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    class NotificationServiceTest {
        @Mock NotificationRepository notificationRepository;
        @Mock UserRepository userRepository;
        @InjectMocks NotificationServiceImpl service;

        private Notification notif(User u) {
            Notification n = mock(Notification.class);
            when(n.getUser()).thenReturn(u);
            return n;
        }

        @Test
        void getUserNotifications_unreadOnly_usesStatusQuery() {
            User u = mock(User.class);
            when(u.getUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(u));
            Notification n = notif(u);
            when(notificationRepository.findByUserAndStatus(u, Status.ACTIVE, PAGE))
                    .thenReturn(new PageImpl<>(List.of(n)));
            assertEquals(1, service.getUserNotifications(1L, true, PAGE).getContent().size());
        }

        @Test
        void getUserNotifications_all_usesPlainQuery() {
            User u = mock(User.class);
            when(u.getUserId()).thenReturn(1L);
            when(userRepository.findById(1L)).thenReturn(Optional.of(u));
            Notification n = notif(u);
            when(notificationRepository.findByUser(u, PAGE)).thenReturn(new PageImpl<>(List.of(n)));
            assertEquals(1, service.getUserNotifications(1L, false, PAGE).getContent().size());
        }

        @Test
        void markAsRead_owner_marksInactive() {
            User u = mock(User.class);
            when(u.getUserId()).thenReturn(1L);
            Notification n = notif(u);
            when(notificationRepository.findById(2L)).thenReturn(Optional.of(n));
            when(notificationRepository.save(n)).thenReturn(n);
            service.markAsRead(2L, 1L);
            verify(n).setStatus(Status.INACTIVE);
        }

        @Test
        void markAsRead_wrongOwner_throwsIsolation() {
            User u = mock(User.class);
            when(u.getUserId()).thenReturn(1L);
            Notification n = notif(u);
            when(notificationRepository.findById(2L)).thenReturn(Optional.of(n));
            assertThrows(DataIsolationViolationException.class, () -> service.markAsRead(2L, 999L));
        }

        @Test
        void markAsRead_notFound_throws() {
            when(notificationRepository.findById(2L)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class, () -> service.markAsRead(2L, 1L));
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    class PartnerServiceTest {
        @Mock PartnerRepository partnerRepository;
        @Mock PartnerMapper partnerMapper;
        @Mock CoreTransactionalUtil transUtil;
        @Mock com.cts.repository.UserRepository userRepository;
        @Mock org.springframework.security.crypto.password.PasswordEncoder passwordEncoder;
        @InjectMocks PartnerServiceImpl service;

        @Test
        void registerPartner_validatesCommitsMaps() {
            PartnerRequestDTO req = mock(PartnerRequestDTO.class);
            when(req.getEmail()).thenReturn("p@p.com");
            Partner entity = mock(Partner.class);
            Partner saved = mock(Partner.class);
            PartnerResponseDTO dto = new PartnerResponseDTO();
            when(partnerMapper.toEntity(req)).thenReturn(entity);
            when(transUtil.commitPartner(entity)).thenReturn(saved);
            when(partnerMapper.toResponseDTO(saved)).thenReturn(dto);
            assertSame(dto, service.registerPartner(req));
            verify(transUtil).validateNewPartnerEmail("p@p.com");
        }

        @Test
        void getAllPartners_paginates() {
            Partner partner = mock(Partner.class);
            when(partnerRepository.findAll(PAGE)).thenReturn(new PageImpl<>(List.of(partner)));
            when(partnerMapper.toResponseDTO(partner)).thenReturn(new PartnerResponseDTO());
            assertEquals(1, service.getAllPartners(PAGE).getContent().size());
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    class PassengerDirectoryServiceTest {
        @Mock PassengerProfileRepository profileRepository;
        @Mock PassengerMapper passengerMapper;
        @Mock UserSecurityUtil securityUtil;
        @InjectMocks PassengerDirectoryServiceImpl service;

        @BeforeEach void ctx() { loginAsAdmin(); }
        @AfterEach void clr() { SecurityContextHolder.clearContext(); }

        @Test
        void savePassengerProfile_savesEntity() {
            User customer = mock(User.class);
            PassengerProfileDTO dto = mock(PassengerProfileDTO.class);
            PassengerProfile profile = mock(PassengerProfile.class);
            when(securityUtil.fetchUser(1L)).thenReturn(customer);
            when(passengerMapper.toEntity(dto, customer)).thenReturn(profile);
            service.savePassengerProfile(1L, dto);
            verify(profileRepository).save(profile);
        }

        @Test
        void updatePassengerProfile_checksOwnerAndSaves() {
            PassengerProfile profile = mock(PassengerProfile.class);
            User customer = mock(User.class);
            when(profile.getCustomer()).thenReturn(customer);
            when(customer.getUserId()).thenReturn(1L);
            PassengerProfileDTO dto = mock(PassengerProfileDTO.class);
            when(profileRepository.findById(2L)).thenReturn(Optional.of(profile));
            service.updatePassengerProfile(2L, dto);
            verify(passengerMapper).updateEntityFromDTO(dto, profile);
            verify(profileRepository).save(profile);
        }

        @Test
        void updatePassengerProfile_notFound_throws() {
            when(profileRepository.findById(2L)).thenReturn(Optional.empty());
            assertThrows(ResourceNotFoundException.class,
                    () -> service.updatePassengerProfile(2L, mock(PassengerProfileDTO.class)));
        }

        @Test
        void removePassengerProfile_checksOwnerAndDeletes() {
            PassengerProfile profile = mock(PassengerProfile.class);
            User customer = mock(User.class);
            when(profile.getCustomer()).thenReturn(customer);
            when(customer.getUserId()).thenReturn(1L);
            when(profileRepository.findById(2L)).thenReturn(Optional.of(profile));
            service.removePassengerProfile(2L);
            verify(profileRepository).delete(profile);
        }

        @Test
        void getCustomerDirectoryPool_listsProfiles() {
            User customer = mock(User.class);
            PassengerProfile profile = mock(PassengerProfile.class);
            when(securityUtil.fetchUser(1L)).thenReturn(customer);
            when(profileRepository.findByCustomer(customer)).thenReturn(List.of(profile));
            when(passengerMapper.toDTO(profile)).thenReturn(new PassengerProfileDTO());
            assertEquals(1, service.getCustomerDirectoryPool(1L).size());
        }
    }

    @Nested
    @ExtendWith(MockitoExtension.class)
    @MockitoSettings(strictness = Strictness.LENIENT)
    class AuditLogQueryServiceTest {
        @Mock AuditLogRepository auditLogRepository;
        @Mock UserRepository userRepository;
        @InjectMocks AuditLogQueryServiceImpl service;

        @Test
        void getLogs_withDateFilter_paginatesCorrectly() {
            AuditLog entry = mock(AuditLog.class);
            when(auditLogRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), eq(PAGE)))
                    .thenReturn(new PageImpl<>(List.of(entry)));
            
            var response = service.getLogs(1L, "LOGIN", "USER", 10L, com.cts.enumeration.EventLevel.INFO, java.time.LocalDate.now(), PAGE);
            assertEquals(1, response.getContent().size());
        }

        @Test
        void buildComplianceReport_fromAfterTo_throws() {
            java.time.LocalDate later = java.time.LocalDate.now();
            java.time.LocalDate earlier = later.minusDays(1);
            assertThrows(IllegalArgumentException.class,
                    () -> service.buildComplianceReport(later, earlier));
        }
    }
}
