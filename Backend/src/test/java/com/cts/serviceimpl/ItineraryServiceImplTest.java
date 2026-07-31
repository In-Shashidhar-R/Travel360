package com.cts.serviceimpl;

import com.cts.dto.ItineraryEntryDTO;
import com.cts.entity.*;
import com.cts.enumeration.InventoryType;
import com.cts.enumeration.Status;
import com.cts.repository.BookingRepository;
import com.cts.security.AuthenticatedUserPrincipal;
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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ItineraryServiceImplTest {

    @Mock BookingRepository bookingRepository;
    @Mock UserSecurityUtil userSecurityUtil;
    @InjectMocks ItineraryServiceImpl service;

    private static final Long CUSTOMER_ID = 7L;

    @BeforeEach
    void loginCustomer() {
        AuthenticatedUserPrincipal principal = new AuthenticatedUserPrincipal(
                CUSTOMER_ID, "c@c.com", List.of(new SimpleGrantedAuthority("ROLE_CUSTOMER")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities()));
    }

    @AfterEach
    void clear() { SecurityContextHolder.clearContext(); }

    @Test
    void getMyUpcomingTrips_filtersAndSortsAscending() {
        User me = User.builder().userId(CUSTOMER_ID).build();
        when(userSecurityUtil.fetchUser(CUSTOMER_ID)).thenReturn(me);
        Booking past = bookingFor(flight("DEL", "BOM"), LocalDate.now().minusDays(5));
        Booking future1 = bookingFor(flight("MAA", "BLR"), LocalDate.now().plusDays(2));
        Booking future2 = bookingFor(flight("MAA", "HYD"), LocalDate.now().plusDays(10));
        when(bookingRepository.findByCustomer(me)).thenReturn(List.of(past, future1, future2));

        List<ItineraryEntryDTO> trips = service.getMyUpcomingTrips();
        assertEquals(2, trips.size());
        assertTrue(trips.get(0).getTravelDate().isBefore(trips.get(1).getTravelDate()));
    }

    @Test
    void getMyPastTrips_filtersAndSortsDescending() {
        User me = User.builder().userId(CUSTOMER_ID).build();
        when(userSecurityUtil.fetchUser(CUSTOMER_ID)).thenReturn(me);
        Booking p1 = bookingFor(flight("DEL", "BOM"), LocalDate.now().minusDays(20));
        Booking p2 = bookingFor(flight("DEL", "MAA"), LocalDate.now().minusDays(5));
        Booking future = bookingFor(flight("MAA", "BLR"), LocalDate.now().plusDays(2));
        when(bookingRepository.findByCustomer(me)).thenReturn(List.of(p1, p2, future));

        List<ItineraryEntryDTO> trips = service.getMyPastTrips();
        assertEquals(2, trips.size());
        // sorted descending: most recent past first
        assertTrue(trips.get(0).getTravelDate().isAfter(trips.get(1).getTravelDate()));
    }

    @Test
    void getTripsForCustomer_admin_returnsAllSorted() {
        AuthenticatedUserPrincipal admin = new AuthenticatedUserPrincipal(
                1L, "a@a.com", List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(admin, null, admin.getAuthorities()));

        User customer = User.builder().userId(CUSTOMER_ID).build();
        when(userSecurityUtil.fetchUser(CUSTOMER_ID)).thenReturn(customer);
        when(bookingRepository.findByCustomer(customer))
                .thenReturn(List.of(bookingFor(flight("A", "B"), LocalDate.now().plusDays(1))));

        List<ItineraryEntryDTO> trips = service.getTripsForCustomer(CUSTOMER_ID);
        assertEquals(1, trips.size());
    }

    @Test
    void describeRoute_coversFlightHotelBusCabTour() {
        User me = User.builder().userId(CUSTOMER_ID).build();
        when(userSecurityUtil.fetchUser(CUSTOMER_ID)).thenReturn(me);

        Booking f = bookingFor(flight("DEL", "BOM"), LocalDate.now().plusDays(1));
        Booking h = bookingForHotel(LocalDate.now().plusDays(2), LocalDate.now().plusDays(5));
        Booking b = bookingFor(bus("Chennai", "Bengaluru"), LocalDate.now().plusDays(3));
        Booking c = bookingFor(cab("Coimbatore", "TN"), LocalDate.now().plusDays(4));
        Booking t = bookingFor(tour("Goa Special"), LocalDate.now().plusDays(6));
        when(bookingRepository.findByCustomer(me)).thenReturn(List.of(f, h, b, c, t));

        List<ItineraryEntryDTO> trips = service.getMyUpcomingTrips();
        assertEquals(5, trips.size());

        boolean sawFlight = false, sawHotel = false, sawBus = false, sawCab = false, sawTour = false;
        for (ItineraryEntryDTO entry : trips) {
            switch (entry.getInventoryType()) {
                case "FLIGHT" -> { sawFlight = true; assertTrue(entry.getRouteOrLocation().contains("DEL")); }
                case "HOTEL" -> { sawHotel = true; assertTrue(entry.getRouteOrLocation().contains("Marriott")); }
                case "BUS" -> { sawBus = true; assertTrue(entry.getRouteOrLocation().contains("Chennai")); }
                case "CAB" -> { sawCab = true; assertTrue(entry.getRouteOrLocation().contains("Coimbatore")); }
                case "TOUR_PACKAGE" -> { sawTour = true; assertEquals("Goa Special", entry.getRouteOrLocation()); }
            }
        }
        assertTrue(sawFlight && sawHotel && sawBus && sawCab && sawTour);
    }

    private FlightInventory flight(String from, String to) {
        FlightInventory f = FlightInventory.builder().departureAirport(from).arrivalAirport(to).build();
        f.setItemType(InventoryType.FLIGHT);
        return f;
    }

    private HotelInventory hotelInv() {
        HotelInventory h = HotelInventory.builder()
                .hotelName("Marriott").addressLocation("Beach Rd")
                .district("Pondy").state("TN").country("India").build();
        h.setItemType(InventoryType.HOTEL);
        return h;
    }

    private BusInventory bus(String from, String to) {
        BusInventory b = BusInventory.builder().routeFrom(from).routeTo(to).build();
        b.setItemType(InventoryType.BUS);
        return b;
    }

    private CabInventory cab(String district, String state) {
        CabInventory c = CabInventory.builder().district(district).state(state).build();
        c.setItemType(InventoryType.CAB);
        return c;
    }

    private TourPackageInventory tour(String name) {
        TourPackageInventory t = TourPackageInventory.builder().packageName(name).build();
        t.setItemType(InventoryType.TOUR_PACKAGE);
        return t;
    }

    private Booking bookingFor(Inventory inv, LocalDate when) {
        return Booking.builder()
                .bookingId(1L).customer(User.builder().userId(CUSTOMER_ID).build())
                .inventory(inv)
                .targetTravelDate(when)
                .status(Status.CONFIRMED).totalAmount(1000.0).requestedSeats(1)
                .build();
    }

    private Booking bookingForHotel(LocalDate checkIn, LocalDate checkOut) {
        return Booking.builder()
                .bookingId(2L).customer(User.builder().userId(CUSTOMER_ID).build())
                .inventory(hotelInv())
                .checkInDate(checkIn).checkOutDate(checkOut)
                .status(Status.CONFIRMED).totalAmount(2000.0).requestedSeats(1)
                .build();
    }
}
