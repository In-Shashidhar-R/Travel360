package com.cts.serviceimpl;

import com.cts.dto.ItineraryEntryDTO;
import com.cts.entity.*;
import com.cts.repository.BookingRepository;
import com.cts.security.SecurityUtil;
import com.cts.service.ItineraryService;
import com.cts.util.UserSecurityUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ItineraryServiceImpl implements ItineraryService {

    private final BookingRepository bookingRepository;
    private final UserSecurityUtil userSecurityUtil;

    @Override
    @Transactional(readOnly = true)
    public List<ItineraryEntryDTO> getMyUpcomingTrips() {
        return filteredTripsFor(SecurityUtil.getCurrentUserId(), /* upcomingOnly */ true);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItineraryEntryDTO> getMyPastTrips() {
        return filteredTripsFor(SecurityUtil.getCurrentUserId(), /* upcomingOnly */ false);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ItineraryEntryDTO> getTripsForCustomer(Long customerId) {
        SecurityUtil.assertSelfOrAdmin(customerId);
        User customer = userSecurityUtil.fetchUser(customerId);
        return bookingRepository.findByCustomer(customer).stream()
                .map(this::toEntry)
                .sorted(Comparator.comparing(ItineraryEntryDTO::getTravelDate,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }


    private List<ItineraryEntryDTO> filteredTripsFor(Long customerId, boolean upcomingOnly) {
        User customer = userSecurityUtil.fetchUser(customerId);
        LocalDate today = LocalDate.now();

        return bookingRepository.findByCustomer(customer).stream()
                .map(this::toEntry)
                .filter(e -> e.getTravelDate() != null)
                .filter(e -> upcomingOnly
                        ? !e.getTravelDate().isBefore(today)
                        :  e.getTravelDate().isBefore(today))
                .sorted(upcomingOnly
                        ? Comparator.comparing(ItineraryEntryDTO::getTravelDate)
                        : Comparator.comparing(ItineraryEntryDTO::getTravelDate).reversed())
                .toList();
    }

    private ItineraryEntryDTO toEntry(Booking b) {
        Inventory inv = b.getInventory();
        String type = (inv != null) ? inv.getItemType().name() : "UNKNOWN";

        LocalDate travelDate = (b.getCheckInDate() != null) ? b.getCheckInDate() : b.getTargetTravelDate();
        LocalDate endDate    = b.getCheckOutDate();

        String routeOrLocation = describeRoute(inv);

        return ItineraryEntryDTO.builder()
                .bookingId(b.getBookingId())
                .inventoryType(type)
                .status(b.getStatus() != null ? b.getStatus().name() : null)
                .travelDate(travelDate)
                .endDate(endDate)
                .routeOrLocation(routeOrLocation)
                .travellers(b.getRequestedSeats())
                .totalAmount(b.getTotalAmount())
                .build();
    }

    private String describeRoute(Inventory inv) {
        if (inv == null) return null;
        if (inv instanceof FlightInventory f)      return safe(f.getDepartureAirport()) + " → " + safe(f.getArrivalAirport());
        if (inv instanceof BusInventory bus)       return safe(bus.getRouteFrom()) + " → " + safe(bus.getRouteTo());
        if (inv instanceof CabInventory cab)       return safe(cab.getDistrict()) + ", " + safe(cab.getState());
        if (inv instanceof HotelInventory h)       return safe(h.getHotelName()) + " — " + safe(h.getAddressLocation());
        if (inv instanceof TourPackageInventory t) return t.getPackageName();
        return null;
    }

    private String safe(String s) { return s == null ? "" : s; }
}
