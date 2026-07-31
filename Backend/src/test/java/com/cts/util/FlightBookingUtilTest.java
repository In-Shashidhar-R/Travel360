package com.cts.util;

import com.cts.entity.FlightInventory;
import com.cts.entity.HotelInventory;
import com.cts.entity.SeatTierCapacity;
import com.cts.enumeration.InventoryType;
import com.cts.enumeration.SeatType;
import com.cts.enumeration.Status;
import com.cts.exception.BookingCapacityExhaustedException;
import com.cts.exception.InvalidTimelineException;
import com.cts.exception.InventoryTypeMismatchException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.BookingRepository;
import com.cts.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FlightBookingUtilTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private DynamicPricingEngine pricingEngine;

    @InjectMocks
    private FlightBookingUtil flightBookingUtil;

    private FlightInventory flightWithTier(SeatType type, int allocated, double multiplier) {
        SeatTierCapacity tier = SeatTierCapacity.builder()
                .seatType(type)
                .totalSeatsAllocated(allocated)
                .priceMultiplier(multiplier)
                .build();
        return FlightInventory.builder()
                .flightNumber("AI-1")
                .airlineName("TestAir")
                .departureAirport("MAA")
                .arrivalAirport("DEL")
                .isConnecting(false)
                .startTime("10:00")
                .endTime("12:00")
                .numberOfHours(2)
                .seatTiers(List.of(tier))
                .inventoryId(1L)
                .itemType(InventoryType.FLIGHT)
                .basePricePerUnit(1000.0)
                .status(Status.ACTIVE)
                .build();
    }

    @Test
    void fetchFlight_notFound_throwsResourceNotFound() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> flightBookingUtil.fetchFlight(1L));
    }

    @Test
    void fetchFlight_wrongType_throwsTypeMismatch() {
        HotelInventory hotel = HotelInventory.builder()
                .hotelName("H").roomType("D").totalSeats(10).starRating(3)
                .inventoryId(1L).itemType(InventoryType.HOTEL)
                .basePricePerUnit(500.0).status(Status.ACTIVE).build();
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(hotel));
        assertThrows(InventoryTypeMismatchException.class, () -> flightBookingUtil.fetchFlight(1L));
    }

    @Test
    void calculateFlightCost_tierNotConfigured_throwsInvalidTimeline() {
        FlightInventory flight = flightWithTier(SeatType.ECONOMY, 100, 1.0);
        assertThrows(InvalidTimelineException.class,
                () -> flightBookingUtil.calculateFlightCost(flight, SeatType.BUSINESS, 1, LocalDate.now().plusDays(10)));
    }

    @Test
    void calculateFlightCost_capacityExhausted_throws() {
        FlightInventory flight = flightWithTier(SeatType.ECONOMY, 10, 1.0);
        when(bookingRepository.getFilledSeatsCountForDateAndSeatType(eq(flight), any(), eq("ECONOMY"))).thenReturn(8);
        assertThrows(BookingCapacityExhaustedException.class,
                () -> flightBookingUtil.calculateFlightCost(flight, SeatType.ECONOMY, 5, LocalDate.now().plusDays(10)));
    }

    @Test
    void calculateFlightCost_happyPath_returnsBaseTimesMultiplierTimesCount() {
        FlightInventory flight = flightWithTier(SeatType.ECONOMY, 100, 2.0);
        LocalDate travelDate = LocalDate.now().plusDays(10);
        when(bookingRepository.getFilledSeatsCountForDateAndSeatType(eq(flight), any(), eq("ECONOMY"))).thenReturn(0);
        when(pricingEngine.calculateDynamicUnitPrice(eq(flight), any(), eq(0))).thenReturn(1000.0);

        double cost = flightBookingUtil.calculateFlightCost(flight, SeatType.ECONOMY, 2, travelDate);

        assertEquals(1000.0 * 2.0 * 2, cost, 0.001);
    }
}
