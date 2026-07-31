package com.cts.serviceimpl;

import com.cts.dto.*;
import com.cts.entity.*;
import com.cts.enumeration.SeatType;
import com.cts.mapper.BookingMapper;
import com.cts.repository.BookingRepository;
import com.cts.security.AuthenticatedUserPrincipal;
import com.cts.util.*;
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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class BookingServiceImplTest {

    @Mock private BookingRepository bookingRepository;
    @Mock private BookingMapper bookingMapper;
    @Mock private FlightBookingUtil flightUtil;
    @Mock private HotelBookingUtil hotelUtil;
    @Mock private TransitBookingUtil transitUtil;
    @Mock private UserSecurityUtil securityUtil;
    @Mock private CoreTransactionalUtil coreUtil;
    @Mock private com.cts.service.ComplaintService complaintService;

    @InjectMocks private BookingServiceImpl service;

    private final Pageable pageable = PageRequest.of(0, 10);
    private final LocalDate date = LocalDate.of(2026, 7, 1);

    @BeforeEach
    void adminContext() {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(
                99L, "admin@a.com", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void clear() {
        SecurityContextHolder.clearContext();
    }

    private User user() {
        User u = mock(User.class);
        when(u.getUserId()).thenReturn(1L);
        return u;
    }

    @Test
    void bookFlight_runsPipelineAndMaps() {
        FlightBookingRequestDTO req = mock(FlightBookingRequestDTO.class);
        when(req.getCustomerId()).thenReturn(1L);
        when(req.getInventoryId()).thenReturn(10L);
        when(req.getChosenSeatType()).thenReturn(SeatType.ECONOMY);
        when(req.getPassengerProfileIds()).thenReturn(List.of(1L));
        when(req.getTargetTravelDate()).thenReturn(date);
        User u = user();
        FlightInventory flight = mock(FlightInventory.class);
        Booking booking = mock(Booking.class);
        Booking committed = mock(Booking.class);
        FlightBookingResponseDTO dto = new FlightBookingResponseDTO();
        when(securityUtil.fetchUser(1L)).thenReturn(u);
        when(flightUtil.fetchFlight(10L)).thenReturn(flight);
        when(securityUtil.fetchAndValidateProfiles(anyList(), eq(1L))).thenReturn(List.of(mock(PassengerProfile.class)));
        when(flightUtil.calculateFlightCost(eq(flight), eq(SeatType.ECONOMY), anyInt(), eq(date))).thenReturn(500.0);
        when(bookingMapper.toFlightEntity(eq(u), eq(flight), anyInt(), eq(date), eq(500.0), anyList())).thenReturn(booking);
        when(coreUtil.executeBookingPipeline(eq(booking), eq(500.0), eq(date), eq("FLIGHT"))).thenReturn(committed);
        when(bookingMapper.toFlightBookingResponse(committed)).thenReturn(dto);

        assertSame(dto, service.bookFlight(req));
        verify(booking).setChosenSeatType("ECONOMY");
    }

    @Test
    void bookHotel_runsPipelineAndMaps() {
        HotelBookingRequestDTO req = mock(HotelBookingRequestDTO.class);
        when(req.getCustomerId()).thenReturn(1L);
        when(req.getInventoryId()).thenReturn(20L);
        when(req.getPassengerProfileIds()).thenReturn(List.of());
        when(req.getCheckInDate()).thenReturn(date);
        when(req.getCheckOutDate()).thenReturn(date.plusDays(2));
        when(req.getRequestedRooms()).thenReturn(2);
        User u = user();
        HotelInventory hotel = mock(HotelInventory.class);
        Booking booking = mock(Booking.class);
        Booking committed = mock(Booking.class);
        HotelBookingResponseDTO dto = new HotelBookingResponseDTO();
        when(securityUtil.fetchUser(1L)).thenReturn(u);
        when(hotelUtil.fetchHotel(20L)).thenReturn(hotel);
        when(securityUtil.fetchOptionalProfiles(anyList(), eq(1L))).thenReturn(List.of());
        when(hotelUtil.calculateHotelCost(eq(hotel), eq(date), eq(date.plusDays(2)), eq(2))).thenReturn(800.0);
        when(bookingMapper.toHotelEntity(eq(u), eq(hotel), eq(2), eq(date), eq(date.plusDays(2)), eq(800.0), anyList())).thenReturn(booking);
        when(coreUtil.executeBookingPipeline(eq(booking), eq(800.0), eq(date), eq("HOTEL"))).thenReturn(committed);
        when(bookingMapper.toHotelBookingResponse(committed)).thenReturn(dto);

        assertSame(dto, service.bookHotel(req));
    }

    @Test
    void bookBus_runsPipelineAndMaps() {
        BusBookingRequestDTO req = mock(BusBookingRequestDTO.class);
        when(req.getCustomerId()).thenReturn(1L);
        when(req.getInventoryId()).thenReturn(30L);
        when(req.getChosenSeatType()).thenReturn(SeatType.AC_SLEEPER);
        when(req.getPassengerProfileIds()).thenReturn(List.of(1L));
        when(req.getTargetTravelDate()).thenReturn(date);
        when(req.getPickupLocation()).thenReturn("A");
        when(req.getDropoffLocation()).thenReturn("B");
        User u = user();
        BusInventory bus = mock(BusInventory.class);
        Booking booking = mock(Booking.class);
        Booking committed = mock(Booking.class);
        BusBookingResponseDTO dto = new BusBookingResponseDTO();
        when(securityUtil.fetchUser(1L)).thenReturn(u);
        when(transitUtil.fetchBus(30L)).thenReturn(bus);
        when(securityUtil.fetchAndValidateProfiles(anyList(), eq(1L))).thenReturn(List.of(mock(PassengerProfile.class)));
        when(transitUtil.calculateBusCost(eq(bus), eq(SeatType.AC_SLEEPER), anyInt(), eq(date))).thenReturn(300.0);
        when(bookingMapper.toBusEntity(eq(u), eq(bus), anyInt(), eq(date), eq("A"), eq("B"), eq(300.0), anyList())).thenReturn(booking);
        when(coreUtil.executeBookingPipeline(eq(booking), eq(300.0), eq(date), eq("BUS"))).thenReturn(committed);
        when(bookingMapper.toBusBookingResponse(committed)).thenReturn(dto);

        assertSame(dto, service.bookBus(req));
        verify(booking).setChosenSeatType("AC_SLEEPER");
    }

    @Test
    void bookCab_runsPipelineAndMaps() {
        CabBookingRequestDTO req = mock(CabBookingRequestDTO.class);
        when(req.getCustomerId()).thenReturn(1L);
        when(req.getInventoryId()).thenReturn(40L);
        when(req.getPassengerProfileIds()).thenReturn(List.of(1L));
        when(req.getTargetTravelDate()).thenReturn(date);
        when(req.getDistrict()).thenReturn("Dist");
        when(req.getState()).thenReturn("State");
        when(req.getPickupLocation()).thenReturn("P");
        when(req.getDropoffLocation()).thenReturn("D");
        User u = user();
        CabInventory cab = mock(CabInventory.class);
        Booking booking = mock(Booking.class);
        Booking committed = mock(Booking.class);
        CabBookingResponseDTO dto = new CabBookingResponseDTO();
        when(securityUtil.fetchUser(1L)).thenReturn(u);
        when(transitUtil.fetchAndValidateCab(40L, "Dist", "State")).thenReturn(cab);
        when(securityUtil.fetchAndValidateProfiles(anyList(), eq(1L))).thenReturn(List.of(mock(PassengerProfile.class)));
        when(transitUtil.calculateCabCost(eq(cab), eq(date))).thenReturn(250.0);
        when(bookingMapper.toCabEntity(eq(u), eq(cab), eq(date), eq("P"), eq("D"), eq(250.0), anyList())).thenReturn(booking);
        when(coreUtil.executeBookingPipeline(eq(booking), eq(250.0), eq(date), eq("CAB"))).thenReturn(committed);
        when(bookingMapper.toCabBookingResponse(committed)).thenReturn(dto);

        assertSame(dto, service.bookCab(req));
    }

    @Test
    void bookTour_runsPipelineAndMaps() {
        TourBookingRequestDTO req = mock(TourBookingRequestDTO.class);
        when(req.getCustomerId()).thenReturn(1L);
        when(req.getInventoryId()).thenReturn(50L);
        when(req.getPassengerProfileIds()).thenReturn(List.of(1L));
        when(req.getTargetTravelDate()).thenReturn(date);
        when(req.getNumberOfPersons()).thenReturn(3);
        User u = user();
        TourPackageInventory tour = mock(TourPackageInventory.class);
        Booking booking = mock(Booking.class);
        Booking committed = mock(Booking.class);
        TourBookingResponseDTO dto = new TourBookingResponseDTO();
        when(securityUtil.fetchUser(1L)).thenReturn(u);
        when(transitUtil.fetchTour(50L)).thenReturn(tour);
        when(securityUtil.fetchAndValidateProfiles(anyList(), eq(1L))).thenReturn(List.of(mock(PassengerProfile.class)));
        when(transitUtil.calculateTourCost(eq(tour), eq(3), anyInt(), eq(date))).thenReturn(900.0);
        when(bookingMapper.toTourEntity(eq(u), eq(tour), eq(3), eq(date), eq(900.0), anyList())).thenReturn(booking);
        when(coreUtil.executeBookingPipeline(eq(booking), eq(900.0), eq(date), eq("TOUR_PACKAGE"))).thenReturn(committed);
        when(bookingMapper.toTourBookingResponse(committed)).thenReturn(dto);

        assertSame(dto, service.bookTour(req));
    }

    @Test
    void cancelBooking_delegatesToPipeline() {
        PartialCancelRequestDTO req = mock(PartialCancelRequestDTO.class);
        when(req.getCustomerId()).thenReturn(1L);
        when(req.getPassengerProfileIdsToCancel()).thenReturn(List.of(2L));
        Booking booking = mock(Booking.class);
        InvoiceCancelResponseDTO dto = InvoiceCancelResponseDTO.builder().build();
        when(coreUtil.fetchBooking(5L)).thenReturn(booking);
        when(coreUtil.executeCancellationPipeline(booking, 1L, List.of(2L))).thenReturn(dto);

        assertSame(dto, service.cancelBooking(5L, req));
    }

    @Test
    void cancelEntireBooking_delegatesToFullPipeline() {
        Booking booking = mock(Booking.class);
        InvoiceCancelResponseDTO dto = InvoiceCancelResponseDTO.builder().build();
        when(coreUtil.fetchBooking(5L)).thenReturn(booking);
        when(coreUtil.executeFullCancellationPipeline(booking, 1L)).thenReturn(dto);

        assertSame(dto, service.cancelEntireBooking(5L, 1L, null));
    }

    @Test
    void getBookingById_checksOwnerThenMaps() {
        Booking booking = mock(Booking.class);
        User owner = user();
        when(booking.getCustomer()).thenReturn(owner);
        Object dto = new Object();
        when(coreUtil.fetchBooking(5L)).thenReturn(booking);
        when(bookingMapper.toGenericBookingResponseDTO(booking)).thenReturn(dto);

        assertSame(dto, service.getBookingById(5L));
    }

    @Test
    void getAllBookings_paginates() {
        Booking booking = mock(Booking.class);
        Page<Booking> page = new PageImpl<>(List.of(booking));
        when(bookingRepository.findAll(pageable)).thenReturn(page);
        when(bookingMapper.toGenericBookingResponseDTO(booking)).thenReturn(new Object());

        PageResponse<Object> result = service.getAllBookings(pageable);
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getCustomerBookings_paginatesForUser() {
        User u = user();
        Booking booking = mock(Booking.class);
        Page<Booking> page = new PageImpl<>(List.of(booking));
        when(securityUtil.fetchUser(1L)).thenReturn(u);
        when(bookingRepository.findByCustomer(u, pageable)).thenReturn(page);
        when(bookingMapper.toGenericBookingResponseDTO(booking)).thenReturn(new Object());

        PageResponse<Object> result = service.getCustomerBookings(1L, pageable);
        assertEquals(1, result.getContent().size());
    }
}
