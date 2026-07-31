package com.cts.serviceimpl;

import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.enumeration.BookingRequestStatus;
import com.cts.enumeration.InventoryType;
import com.cts.exception.DataIsolationViolationException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.BookingRequestRepository;
import com.cts.security.AuthenticatedUserPrincipal;
import com.cts.service.BookingService;
import com.cts.util.InventoryManagementUtil;
import com.cts.util.UserSecurityUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingRequestServiceImplTest {

    @Mock BookingRequestRepository bookingRequestRepository;
    @Mock UserSecurityUtil userSecurityUtil;
    @Mock InventoryManagementUtil inventoryUtil;
    @Mock BookingService bookingService;
    @InjectMocks BookingRequestServiceImpl service;

    private static final Long CUSTOMER_ID = 7L;
    private static final Long AGENT_ID = 9L;
    private static final Long INVENTORY_ID = 100L;

    @BeforeEach
    void setupContext() {
        loginAs(CUSTOMER_ID, "ROLE_CUSTOMER");
    }

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createRequest_ok_returnsPersistedDto() {
        BookingRequestCreateDTO req = new BookingRequestCreateDTO();
        req.setInventoryId(INVENTORY_ID);
        req.setCustomerRequirements("Need vegetarian meals");

        User customer = User.builder().userId(CUSTOMER_ID).name("Riya").email("r@r.com").build();
        User agent = User.builder().userId(AGENT_ID).name("Agent").email("a@a.com").build();
        TourPackageInventory pkg = TourPackageInventory.builder().packageName("Goa").travelAgent(agent).build();
        pkg.setInventoryId(INVENTORY_ID);
        pkg.setItemType(InventoryType.TOUR_PACKAGE);

        when(userSecurityUtil.fetchUser(CUSTOMER_ID)).thenReturn(customer);
        when(inventoryUtil.fetchInventory(INVENTORY_ID)).thenReturn(pkg);
        when(bookingRequestRepository.save(any(BookingRequest.class))).thenAnswer(inv -> {
            BookingRequest br = inv.getArgument(0);
            br.setRequestId(42L);
            return br;
        });

        BookingRequestResponseDTO dto = service.createRequest(req);

        assertEquals(42L, dto.getRequestId());
        assertEquals(BookingRequestStatus.PENDING, dto.getStatus());
        assertEquals(CUSTOMER_ID, dto.getCustomerId());
        assertEquals(AGENT_ID, dto.getAssignedAgentId());
        assertEquals("Goa", dto.getPackageName());
    }

    @Test
    void createRequest_nonTourInventory_rejects() {
        BookingRequestCreateDTO req = new BookingRequestCreateDTO();
        req.setInventoryId(INVENTORY_ID);
        FlightInventory flight = FlightInventory.builder().build();
        flight.setInventoryId(INVENTORY_ID);
        flight.setItemType(InventoryType.FLIGHT);

        when(userSecurityUtil.fetchUser(CUSTOMER_ID)).thenReturn(User.builder().userId(CUSTOMER_ID).build());
        when(inventoryUtil.fetchInventory(INVENTORY_ID)).thenReturn(flight);

        assertThrows(IllegalArgumentException.class, () -> service.createRequest(req));
    }

    @Test
    void createRequest_tourWithoutAgent_rejects() {
        BookingRequestCreateDTO req = new BookingRequestCreateDTO();
        req.setInventoryId(INVENTORY_ID);
        TourPackageInventory pkg = TourPackageInventory.builder().packageName("Goa").build();   // no agent
        pkg.setInventoryId(INVENTORY_ID);
        pkg.setItemType(InventoryType.TOUR_PACKAGE);

        when(userSecurityUtil.fetchUser(CUSTOMER_ID)).thenReturn(User.builder().userId(CUSTOMER_ID).build());
        when(inventoryUtil.fetchInventory(INVENTORY_ID)).thenReturn(pkg);

        assertThrows(IllegalStateException.class, () -> service.createRequest(req));
    }

    @Test
    void acceptRequest_assignedAgent_movesToApproved() {
        loginAs(AGENT_ID, "ROLE_TRAVEL_AGENT");
        BookingRequest entity = pendingRequest();
        when(bookingRequestRepository.findById(5L)).thenReturn(Optional.of(entity));
        when(bookingRequestRepository.save(entity)).thenReturn(entity);

        BookingRequestDecisionDTO decision = new BookingRequestDecisionDTO();
        decision.setAgentNotes("Sure, let's do it");

        BookingRequestResponseDTO dto = service.acceptRequest(5L, decision);
        assertEquals(BookingRequestStatus.APPROVED, dto.getStatus());
        assertEquals("Sure, let's do it", dto.getAgentNotes());
    }

    @Test
    void rejectRequest_assignedAgent_movesToRejected() {
        loginAs(AGENT_ID, "ROLE_TRAVEL_AGENT");
        BookingRequest entity = pendingRequest();
        when(bookingRequestRepository.findById(5L)).thenReturn(Optional.of(entity));
        when(bookingRequestRepository.save(entity)).thenReturn(entity);

        BookingRequestDecisionDTO decision = new BookingRequestDecisionDTO();
        decision.setAgentNotes("Sold out");

        BookingRequestResponseDTO dto = service.rejectRequest(5L, decision);
        assertEquals(BookingRequestStatus.REJECTED, dto.getStatus());
    }

    @Test
    void acceptRequest_nonAssignedAgent_throwsIsolation() {
        loginAs(999L, "ROLE_TRAVEL_AGENT");
        when(bookingRequestRepository.findById(5L)).thenReturn(Optional.of(pendingRequest()));
        assertThrows(DataIsolationViolationException.class,
                () -> service.acceptRequest(5L, new BookingRequestDecisionDTO()));
    }

    @Test
    void acceptRequest_nonPendingStatus_throwsState() {
        loginAs(AGENT_ID, "ROLE_TRAVEL_AGENT");
        BookingRequest entity = pendingRequest();
        entity.setStatus(BookingRequestStatus.APPROVED);
        when(bookingRequestRepository.findById(5L)).thenReturn(Optional.of(entity));
        assertThrows(IllegalStateException.class,
                () -> service.acceptRequest(5L, new BookingRequestDecisionDTO()));
    }

    @Test
    void acceptRequest_notFound_throws() {
        loginAs(AGENT_ID, "ROLE_TRAVEL_AGENT");
        when(bookingRequestRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> service.acceptRequest(5L, new BookingRequestDecisionDTO()));
    }

    @Test
    void completeRequestByBooking_callsBookingServiceAndClosesRequest() {
        loginAs(AGENT_ID, "ROLE_TRAVEL_AGENT");
        BookingRequest entity = pendingRequest();
        entity.setStatus(BookingRequestStatus.APPROVED);
        when(bookingRequestRepository.findById(5L)).thenReturn(Optional.of(entity));
        when(bookingRequestRepository.save(entity)).thenReturn(entity);

        TourBookingResponseDTO bookingDto = new TourBookingResponseDTO();
        bookingDto.setBookingId(77L);
        when(bookingService.bookTour(any(TourBookingRequestDTO.class))).thenReturn(bookingDto);

        BookingRequestResponseDTO dto = service.completeRequestByBooking(5L, new TourBookingRequestDTO());
        assertEquals(BookingRequestStatus.COMPLETED, dto.getStatus());
        assertEquals(77L, dto.getResultingBookingId());
        verify(bookingService).bookTour(any(TourBookingRequestDTO.class));
    }

    @Test
    void completeRequestByBooking_notApproved_throwsState() {
        loginAs(AGENT_ID, "ROLE_TRAVEL_AGENT");
        when(bookingRequestRepository.findById(5L)).thenReturn(Optional.of(pendingRequest()));
        assertThrows(IllegalStateException.class,
                () -> service.completeRequestByBooking(5L, new TourBookingRequestDTO()));
    }

    @Test
    void getRequestById_asCustomerOwner_returns() {
        BookingRequest entity = pendingRequest();
        when(bookingRequestRepository.findById(5L)).thenReturn(Optional.of(entity));
        BookingRequestResponseDTO dto = service.getRequestById(5L);
        assertEquals(BookingRequestStatus.PENDING, dto.getStatus());
    }

    @Test
    void getRequestById_asStranger_throwsIsolation() {
        loginAs(444L, "ROLE_CUSTOMER");
        when(bookingRequestRepository.findById(5L)).thenReturn(Optional.of(pendingRequest()));
        assertThrows(DataIsolationViolationException.class, () -> service.getRequestById(5L));
    }

    @Test
    void getRequestById_notFound_throws() {
        when(bookingRequestRepository.findById(5L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.getRequestById(5L));
    }

    @Test
    void listMyCustomerRequests_returnsPagedDtos() {
        User me = User.builder().userId(CUSTOMER_ID).build();
        when(userSecurityUtil.fetchUser(CUSTOMER_ID)).thenReturn(me);
        Page<BookingRequest> page = new PageImpl<>(List.of(pendingRequest()));
        when(bookingRequestRepository.findByCustomer(eq(me), any(Pageable.class))).thenReturn(page);
        PageResponse<BookingRequestResponseDTO> result = service.listMyCustomerRequests(Pageable.unpaged());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void listAssignedRequests_returnsPagedDtos() {
        loginAs(AGENT_ID, "ROLE_TRAVEL_AGENT");
        User me = User.builder().userId(AGENT_ID).build();
        when(userSecurityUtil.fetchUser(AGENT_ID)).thenReturn(me);
        Page<BookingRequest> page = new PageImpl<>(List.of(pendingRequest()));
        when(bookingRequestRepository.findByAssignedAgent(eq(me), any(Pageable.class))).thenReturn(page);
        PageResponse<BookingRequestResponseDTO> result = service.listAssignedRequests(Pageable.unpaged());
        assertEquals(1, result.getContent().size());
    }

    private BookingRequest pendingRequest() {
        User customer = User.builder().userId(CUSTOMER_ID).name("Riya").email("r@r.com").build();
        User agent = User.builder().userId(AGENT_ID).name("Agent").build();
        TourPackageInventory pkg = TourPackageInventory.builder().packageName("Goa").build();
        pkg.setInventoryId(INVENTORY_ID);
        pkg.setItemType(InventoryType.TOUR_PACKAGE);
        return BookingRequest.builder()
                .requestId(5L)
                .customer(customer)
                .assignedAgent(agent)
                .inventory(pkg)
                .status(BookingRequestStatus.PENDING)
                .customerRequirements("Custom request")
                .build();
    }

    private void loginAs(Long userId, String authority) {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(
                userId, "u@u.com", List.of(new SimpleGrantedAuthority(authority)));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    private static <T> T eq(T value) { return org.mockito.ArgumentMatchers.eq(value); }
}
