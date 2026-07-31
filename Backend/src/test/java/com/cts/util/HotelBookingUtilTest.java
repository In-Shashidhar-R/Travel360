package com.cts.util;

import com.cts.entity.Booking;
import com.cts.entity.HotelInventory;
import com.cts.enumeration.InventoryType;
import com.cts.enumeration.Status;
import com.cts.exception.BookingCapacityExhaustedException;
import com.cts.exception.InvalidTimelineException;
import com.cts.repository.BookingRepository;
import com.cts.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class HotelBookingUtilTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private DynamicPricingEngine pricingEngine;

    @InjectMocks
    private HotelBookingUtil hotelBookingUtil;

    private HotelInventory hotel(int totalRooms) {
        return HotelInventory.builder()
                .hotelName("TestHotel").roomType("Deluxe").totalSeats(totalRooms).starRating(4)
                .inventoryId(1L).itemType(InventoryType.HOTEL)
                .basePricePerUnit(2000.0).status(Status.ACTIVE).build();
    }

    @Test
    void calculateHotelCost_checkoutNotAfterCheckin_throwsInvalidTimeline() {
        HotelInventory h = hotel(100);
        LocalDate sameDay = LocalDate.now().plusDays(5);
        assertThrows(InvalidTimelineException.class,
                () -> hotelBookingUtil.calculateHotelCost(h, sameDay, sameDay, 1));
    }

    @Test
    void calculateHotelCost_overlappingBookingsExceedCapacity_throws() {
        HotelInventory h = hotel(100);
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = checkIn.plusDays(2);

        Booking existing = Booking.builder()
                .bookingId(99L)
                .requestedSeats(95)
                .checkInDate(checkIn)
                .checkOutDate(checkOut)
                .status(Status.CONFIRMED)
                .build();
        when(bookingRepository.findActiveHotelBookingsOverlapping(any(), any(), any()))
                .thenReturn(List.of(existing));

        assertThrows(BookingCapacityExhaustedException.class,
                () -> hotelBookingUtil.calculateHotelCost(h, checkIn, checkOut, 10)); // 95 + 10 > 100
    }

    @Test
    void calculateHotelCost_happyPath_returnsUnitPriceTimesRoomsTimesNights() {
        HotelInventory h = hotel(100);
        LocalDate checkIn = LocalDate.now().plusDays(10);
        LocalDate checkOut = checkIn.plusDays(2); // 2 nights

        when(bookingRepository.findActiveHotelBookingsOverlapping(any(), any(), any())).thenReturn(List.of());
        when(pricingEngine.calculateDynamicUnitPrice(any(), any(), org.mockito.ArgumentMatchers.eq(0)))
                .thenReturn(1000.0);

        double cost = hotelBookingUtil.calculateHotelCost(h, checkIn, checkOut, 2);

        assertEquals(1000.0 * 2 * 2, cost, 0.001); // unit * rooms * nights
    }
}
