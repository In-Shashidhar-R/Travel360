package com.cts.util;

import com.cts.entity.FlightInventory;
import com.cts.entity.Inventory;
import com.cts.entity.SeatTierCapacity;
import com.cts.enumeration.InventoryType;
import com.cts.enumeration.SeatType;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class FlightBookingUtil {

    private final InventoryRepository inventoryRepository;
    private final BookingRepository bookingRepository;
    private final DynamicPricingEngine pricingEngine;

    public FlightInventory fetchFlight(Long id) {
        Inventory inv = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight inventory not found with ID: " + id));
        if (inv.getItemType() != InventoryType.FLIGHT) {
            throw new InventoryTypeMismatchException("Selected inventory ID " + id + " is not a flight.");
        }
        InventoryAvailabilityGuard.assertBookable(inv);
        return (FlightInventory) inv;
    }

    public double calculateFlightCost(FlightInventory flight, SeatType seatType, int seatCount, LocalDate travelDate) {
        SeatTierCapacity tier = flight.getSeatTiers().stream()
                .filter(t -> t.getSeatType() == seatType)
                .findFirst()
                .orElseThrow(() -> new InvalidTimelineException(
                        "Seat tier " + seatType + " is not configured for this flight."));

        int filled = bookingRepository.getFilledSeatsCountForDateAndSeatType(flight, travelDate, seatType.name());
        if (filled + seatCount > tier.getTotalSeatsAllocated()) {
            log.warn("Flight {} tier {} sold out: filled={}, requested={}, allocated={}",
                    flight.getInventoryId(), seatType, filled, seatCount, tier.getTotalSeatsAllocated());
            throw new BookingCapacityExhaustedException(
                    String.format("Allocation overlap: %s class is sold out for this flight.", seatType));
        }
        double basePrice = pricingEngine.calculateDynamicUnitPrice(flight, travelDate, filled);
        return basePrice * tier.getPriceMultiplier() * seatCount;
    }
}
