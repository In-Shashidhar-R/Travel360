package com.cts.util;

import com.cts.entity.Booking;
import com.cts.entity.HotelInventory;
import com.cts.entity.Inventory;
import com.cts.enumeration.InventoryType;
import com.cts.exception.BookingCapacityExhaustedException;
import com.cts.exception.InvalidTimelineException;
import com.cts.exception.InventoryTypeMismatchException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.BookingRepository;
import com.cts.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class HotelBookingUtil {

    private final InventoryRepository inventoryRepository;
    private final BookingRepository bookingRepository;
    private final DynamicPricingEngine pricingEngine;

    public HotelInventory fetchHotel(Long id) {
        Inventory inv = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel inventory not found with ID: " + id));
        if (inv.getItemType() != InventoryType.HOTEL) {
            throw new InventoryTypeMismatchException("Selected inventory ID " + id + " is not a hotel room.");
        }
        InventoryAvailabilityGuard.assertBookable(inv);
        return (HotelInventory) inv;
    }

    public double calculateHotelCost(HotelInventory hotel, LocalDate checkIn, LocalDate checkOut, Integer requestedRooms) {
        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            throw new InvalidTimelineException("Invalid calendar window: check-out date must be after check-in date.");
        }
        int rooms = requestedRooms != null ? requestedRooms : 1;
        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);

        List<Booking> overlapping = bookingRepository.findActiveHotelBookingsOverlapping(hotel, checkIn, checkOut);

        for (LocalDate night = checkIn; night.isBefore(checkOut); night = night.plusDays(1)) {
            final LocalDate currentNight = night;
            int occupied = overlapping.stream()
                    .filter(b -> !currentNight.isBefore(b.getCheckInDate()) && currentNight.isBefore(b.getCheckOutDate()))
                    .mapToInt(b -> b.getRequestedSeats() != null ? b.getRequestedSeats() : 1)
                    .sum();

            if (occupied + rooms > hotel.getTotalSeats()) {
                log.warn("Hotel capacity exceeded for inventory {} on {}: occupied={}, requested={}, capacity={}",
                        hotel.getInventoryId(), currentNight, occupied, rooms, hotel.getTotalSeats());
                throw new BookingCapacityExhaustedException(
                        "Hotel room allocation limit exceeded on date: " + currentNight);
            }
        }
        return pricingEngine.calculateDynamicUnitPrice(hotel, checkIn, 0) * rooms * nights;
    }
}
