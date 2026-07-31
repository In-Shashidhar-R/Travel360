package com.cts.util;
import org.hibernate.Hibernate;

import com.cts.entity.BusInventory;
import com.cts.entity.CabInventory;
import com.cts.entity.Inventory;
import com.cts.entity.SeatTierCapacity;
import com.cts.entity.TourPackageInventory;
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
public class TransitBookingUtil {

    private final InventoryRepository inventoryRepository;
    private final BookingRepository bookingRepository;
    private final DynamicPricingEngine pricingEngine;

    public BusInventory fetchBus(Long id) {
        Inventory inv = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Bus inventory not found with ID: " + id));
        if (inv.getItemType() != InventoryType.BUS) {
            throw new InventoryTypeMismatchException("Selected inventory ID " + id + " is not a bus.");
        }
        InventoryAvailabilityGuard.assertBookable(inv);
        return (BusInventory) inv;
    }

    public CabInventory fetchAndValidateCab(Long id, String district, String state) {
        Inventory inv = inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cab inventory not found with ID: " + id));
        if (inv.getItemType() != InventoryType.CAB) {
            throw new InventoryTypeMismatchException("Selected inventory ID " + id + " is not a cab.");
        }
        InventoryAvailabilityGuard.assertBookable(inv);
        CabInventory cab = (CabInventory) inv;
        if (!cab.getDistrict().equalsIgnoreCase(district) || !cab.getState().equalsIgnoreCase(state)) {
            throw new InvalidTimelineException(
                    String.format("This cab operates only within %s, %s.", cab.getDistrict(), cab.getState()));
        }
        return cab;
    }
    
    

    public TourPackageInventory fetchTour(Long id) {
    	 
        Inventory inv = inventoryRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Tour package inventory not found with ID: " + id));
     
        log.info("Before unproxy: {}", inv.getClass().getName());
     
        inv = (Inventory) Hibernate.unproxy(inv);
     
        log.info("After unproxy: {}", inv.getClass().getName());
     
        if (!(inv instanceof TourPackageInventory)) {
            throw new RuntimeException(
                    "Expected TourPackageInventory but got "
                            + inv.getClass().getName());
        }
     
        InventoryAvailabilityGuard.assertBookable(inv);
     
        return (TourPackageInventory) inv;
    }

    public double calculateBusCost(BusInventory bus, SeatType seatType, int count, LocalDate date) {
        SeatTierCapacity tier = bus.getSeatTiers().stream()
                .filter(t -> t.getSeatType() == seatType)
                .findFirst()
                .orElseThrow(() -> new InvalidTimelineException(
                        "Seat tier " + seatType + " is not configured for this bus."));

        int filled = bookingRepository.getFilledSeatsCountForDateAndSeatType(bus, date, seatType.name());
        if (filled + count > tier.getTotalSeatsAllocated()) {
            log.warn("Bus {} tier {} full: filled={}, requested={}, allocated={}",
                    bus.getInventoryId(), seatType, filled, count, tier.getTotalSeatsAllocated());
            throw new BookingCapacityExhaustedException("Bus seat tier " + seatType + " capacity is full.");
        }
        return pricingEngine.calculateDynamicUnitPrice(bus, date, filled) * tier.getPriceMultiplier() * count;
    }

    public double calculateCabCost(CabInventory cab, LocalDate date) {
        int filled = bookingRepository.getFilledSeatsCountForDate(cab, date);
        if (filled + 1 > cab.getSeaterCount()) {
            log.warn("Cab {} fully booked on {}", cab.getInventoryId(), date);
            throw new BookingCapacityExhaustedException("Cab booking slots are exhausted for this date.");
        }
        return pricingEngine.calculateDynamicUnitPrice(cab, date, filled);
    }

    public double calculateTourCost(TourPackageInventory tour, Integer requestedPersons, int snapshotSize, LocalDate date) {
        if (requestedPersons == null || requestedPersons != snapshotSize) {
            throw new InvalidTimelineException(
                    "Tour headcount does not match the number of passenger profiles supplied.");
        }
        int filled = bookingRepository.getFilledSeatsCountForDate(tour, date);
        return pricingEngine.calculateDynamicUnitPrice(tour, date, filled) * requestedPersons;
    }
}
