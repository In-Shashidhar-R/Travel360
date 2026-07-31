package com.cts.serviceimpl;

import com.cts.dto.ComplaintCreateDTO;
import com.cts.dto.ComplaintResolveDTO;
import com.cts.dto.ComplaintResponseDTO;
import com.cts.dto.PageResponse;
import com.cts.entity.Booking;
import com.cts.entity.Complaint;
import com.cts.entity.User;
import com.cts.enumeration.ComplaintStatus;
import com.cts.exception.DataIsolationViolationException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.BookingRepository;
import com.cts.repository.ComplaintRepository;
import com.cts.repository.UserRepository;
import com.cts.security.AuthenticatedUserPrincipal;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ComplaintServiceImplTest {

    @Mock ComplaintRepository complaintRepository;
    @Mock UserRepository userRepository;
    @Mock BookingRepository bookingRepository;
    @InjectMocks ComplaintServiceImpl service;

    private static final Long CUSTOMER_ID = 7L;

    @BeforeEach
    void loginCustomer() {
        login(CUSTOMER_ID, "ROLE_CUSTOMER");
        when(userRepository.findById(CUSTOMER_ID))
                .thenReturn(Optional.of(User.builder().userId(CUSTOMER_ID).name("Riya").build()));
    }

    @AfterEach
    void clear() { SecurityContextHolder.clearContext(); }

    @Test
    void raiseComplaint_general_savesOpenComplaint() {
        ComplaintCreateDTO req = new ComplaintCreateDTO();
        req.setSubject("Poor service");
        req.setDescription("The bus was late by 3 hours.");
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> {
            Complaint c = i.getArgument(0);
            c.setComplaintId(1L);
            return c;
        });

        ComplaintResponseDTO dto = service.raiseComplaint(req);
        assertEquals(1L, dto.getComplaintId());
        assertEquals(ComplaintStatus.OPEN.name(), dto.getStatus());
        assertEquals(CUSTOMER_ID, dto.getRaisedByUserId());
        assertNull(dto.getRelatedBookingId());
    }

    @Test
    void raiseComplaint_withOwnBooking_attachesBooking() {
        ComplaintCreateDTO req = new ComplaintCreateDTO();
        req.setSubject("Booking issue");
        req.setDescription("Wrong seat assigned.");
        req.setRelatedBookingId(50L);

        Booking booking = Booking.builder()
                .bookingId(50L).customer(User.builder().userId(CUSTOMER_ID).build()).build();
        when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));

        ComplaintResponseDTO dto = service.raiseComplaint(req);
        assertEquals(50L, dto.getRelatedBookingId());
    }

    @Test
    void raiseComplaint_withOthersBooking_throwsIsolation() {
        ComplaintCreateDTO req = new ComplaintCreateDTO();
        req.setSubject("x"); req.setDescription("y"); req.setRelatedBookingId(50L);
        Booking booking = Booking.builder()
                .bookingId(50L).customer(User.builder().userId(999L).build()).build();
        when(bookingRepository.findById(50L)).thenReturn(Optional.of(booking));

        assertThrows(DataIsolationViolationException.class, () -> service.raiseComplaint(req));
    }

    @Test
    void raiseComplaint_missingBooking_throwsNotFound() {
        ComplaintCreateDTO req = new ComplaintCreateDTO();
        req.setSubject("x"); req.setDescription("y"); req.setRelatedBookingId(404L);
        when(bookingRepository.findById(404L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.raiseComplaint(req));
    }

    @Test
    void getComplaints_withDynamicFilters_returnsPaged() {
        when(complaintRepository.findAll(any(Specification.class), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(sample())));

        PageResponse<ComplaintResponseDTO> result = service.getComplaints(CUSTOMER_ID, ComplaintStatus.OPEN, 50L, Pageable.unpaged());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getComplaintById_ownerCustomer_returns() {
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(sample()));
        assertEquals(1L, service.getComplaintById(1L).getComplaintId());
    }

    @Test
    void getComplaintById_strangerCustomer_throwsIsolation() {
        login(444L, "ROLE_CUSTOMER");
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(sample()));
        assertThrows(DataIsolationViolationException.class, () -> service.getComplaintById(1L));
    }

    @Test
    void getComplaintById_complianceOfficer_canView() {
        login(99L, "ROLE_COMPLIANCE_OFFICER");
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(sample()));
        assertEquals(1L, service.getComplaintById(1L).getComplaintId());
    }

    @Test
    void markInProgress_setsStatusAndNote() {
        Complaint c = sample();
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(c));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));
        ComplaintResolveDTO req = new ComplaintResolveDTO();
        req.setResolutionNote("Looking into it");
        ComplaintResponseDTO dto = service.markInProgress(1L, req);
        assertEquals(ComplaintStatus.IN_PROGRESS.name(), dto.getStatus());
        assertEquals("Looking into it", dto.getResolutionNote());
    }

    @Test
    void resolveComplaint_setsResolved() {
        Complaint c = sample();
        when(complaintRepository.findById(1L)).thenReturn(Optional.of(c));
        when(complaintRepository.save(any(Complaint.class))).thenAnswer(i -> i.getArgument(0));
        ComplaintResolveDTO req = new ComplaintResolveDTO();
        req.setResolutionNote("Refund issued");
        ComplaintResponseDTO dto = service.resolveComplaint(1L, req);
        assertEquals(ComplaintStatus.RESOLVED.name(), dto.getStatus());
    }

    @Test
    void resolveComplaint_notFound_throws() {
        when(complaintRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.resolveComplaint(1L, new ComplaintResolveDTO()));
    }

    private Complaint sample() {
        return Complaint.builder()
                .complaintId(1L)
                .raisedBy(User.builder().userId(CUSTOMER_ID).name("Riya").build())
                .subject("Subject").description("Desc")
                .status(ComplaintStatus.OPEN)
                .createdAt(java.time.LocalDateTime.now())
                .build();
    }

    private void login(Long userId, String authority) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(
                userId, "u@u.com", List.of(new SimpleGrantedAuthority(authority)));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }
}