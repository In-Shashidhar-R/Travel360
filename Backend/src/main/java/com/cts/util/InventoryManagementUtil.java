package com.cts.util;
 
import com.cts.entity.BusInventory;
import com.cts.entity.CabInventory;
import com.cts.entity.FlightInventory;
import com.cts.entity.HotelInventory;
import com.cts.entity.Inventory;
import com.cts.entity.SeatTierCapacity;
import com.cts.entity.TourPackageInventory;
import com.cts.enumeration.InventoryType;
import com.cts.enumeration.Status;
import com.cts.exception.InventoryInUseException;
import com.cts.exception.InventoryTypeMismatchException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.BookingRepository;
import com.cts.repository.InventoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.stream.Stream;
 
@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryManagementUtil {
 
    private final InventoryRepository inventoryRepository;
    private final BookingRepository bookingRepository;
    private final DynamicPricingEngine pricingEngine;
 
    public Inventory fetchInventory(Long id) {
        return inventoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inventory not found with ID: " + id));
    }
    public void assertDistinctRoute(String source, String destination) {
        if (source != null && destination != null
&& source.trim().equalsIgnoreCase(destination.trim())) {
            throw new IllegalArgumentException("Source and destination cannot be the same: " + source.trim());
        }
    }
 
    public Inventory fetchInventoryOfType(Long id, InventoryType expectedType) {
        Inventory inv = fetchInventory(id);
        if (inv.getItemType() != expectedType) {
            throw new InventoryTypeMismatchException(
                    "Inventory ID " + id + " is a " + inv.getItemType() + ", not a " + expectedType + ".");
        }
        return inv;
    }
 
    public void deleteInventoryOfType(Long id, InventoryType expectedType) {
        Inventory inv = fetchInventoryOfType(id, expectedType);
        long referencingBookings = bookingRepository.countByInventory(inv);
        if (referencingBookings > 0) {
            throw new InventoryInUseException(
                    "Cannot delete inventory " + id + " because " + referencingBookings
                            + " booking(s) reference it. Deactivate it instead.");
        }
        inventoryRepository.delete(inv);
        log.info("Deleted {} inventory #{}", expectedType, id);
    }
 
    public Inventory updateStatus(Long id, Status status) {
        Inventory inv = fetchInventory(id);
        inv.setStatus(status);
        Inventory saved = inventoryRepository.save(inv);
        log.info("Inventory #{} status changed to {}", id, status);
        return saved;
    }
 
    public <T extends Inventory> T commitInventory(T entity) {
        if (entity.getInventoryId() == null) {
            entity.setStatus(Status.ACTIVE);
        }
        return (T) inventoryRepository.save(entity);
    }
 
    public Stream<Inventory> streamActiveInventories() {
        return inventoryRepository.findAll().stream();
//                .filter(inv -> Status.ACTIVE.equals(inv.getStatus()));
    }
 
    public void applyLiveDynamicPricing(Inventory inv, LocalDate targetDate) {
        if (targetDate == null) return; 
        int liveFilledSeats = 0;
        double componentMultiplier = 1.0;
 
        if (inv instanceof FlightInventory flight && !flight.getSeatTiers().isEmpty()) {
            SeatTierCapacity defaultTier = flight.getSeatTiers().get(0);
            componentMultiplier = defaultTier.getPriceMultiplier();
            liveFilledSeats = bookingRepository.getFilledSeatsCountForDateAndSeatType(flight, targetDate, defaultTier.getSeatType().name());
        } else if (inv instanceof BusInventory bus && !bus.getSeatTiers().isEmpty()) {
            SeatTierCapacity defaultTier = bus.getSeatTiers().get(0);
            componentMultiplier = defaultTier.getPriceMultiplier();
            liveFilledSeats = bookingRepository.getFilledSeatsCountForDateAndSeatType(bus, targetDate, defaultTier.getSeatType().name());
        } else if (inv instanceof CabInventory cab) {
            liveFilledSeats = bookingRepository.getFilledSeatsCountForDate(cab, targetDate);
        }
        double dynamicUnitPrice = pricingEngine.calculateDynamicUnitPrice(inv, targetDate, liveFilledSeats);
        inv.setBasePricePerUnit(dynamicUnitPrice * componentMultiplier); 
    }
 
    public boolean matchesRoute(Inventory inventory, String source, String destination) {
        if (source == null || destination == null) {
            throw new IllegalArgumentException("Source and Destination criteria cannot be empty");
        }
        String src = source.trim().toLowerCase();
        String dest = destination.trim().toLowerCase();
 
        if (inventory instanceof FlightInventory f) {
            return f.getDepartureAirport() != null && f.getArrivalAirport() != null &&
                   f.getDepartureAirport().toLowerCase().contains(src) && f.getArrivalAirport().toLowerCase().contains(dest);
        } 
        if (inventory instanceof BusInventory b) {
            return b.getRouteFrom() != null && b.getRouteTo() != null &&
                   b.getRouteFrom().toLowerCase().contains(src) && b.getRouteTo().toLowerCase().contains(dest);
        }
        return false;
    }
 
    public boolean matchesAdvancedFilters(Inventory inv, String state, String district, String city, 
                                          String source, String destination, Integer capacity, Integer days) {
        switch (inv.getItemType()) {
            case HOTEL -> {
                HotelInventory hotel = (HotelInventory) inv;
                boolean matchesCity = (city == null || city.trim().isEmpty() || hotel.getAddressLocation().toLowerCase().contains(city.trim().toLowerCase()));
                boolean matchesRooms = (capacity == null || hotel.getTotalSeats() >= capacity);
                return matchesCity && matchesRooms;
            }
            case CAB -> {
                CabInventory cab = (CabInventory) inv;
                boolean matchesState = (state == null || state.trim().isEmpty() || cab.getState().equalsIgnoreCase(state.trim()));
                boolean matchesDistrict = (district == null || district.trim().isEmpty() || cab.getDistrict().equalsIgnoreCase(district.trim()));
                boolean matchesSeats = (capacity == null || cab.getSeaterCount() >= capacity);
                return matchesState && matchesDistrict && matchesSeats;
            }
            case FLIGHT -> {
                FlightInventory flight = (FlightInventory) inv;
                boolean matchesSource = (source == null || source.trim().isEmpty() || flight.getDepartureAirport().equalsIgnoreCase(source.trim()));
                boolean matchesDest = (destination == null || destination.trim().isEmpty() || flight.getArrivalAirport().equalsIgnoreCase(destination.trim()));
                boolean matchesSeats = (capacity == null || flight.getTotalSeats() >= capacity);
                return matchesSource && matchesDest && matchesSeats;
            }
            case BUS -> {
                BusInventory bus = (BusInventory) inv;
                boolean matchesSource = (source == null || source.trim().isEmpty() || bus.getRouteFrom().equalsIgnoreCase(source.trim()));
                boolean matchesDest = (destination == null || destination.trim().isEmpty() || bus.getRouteTo().equalsIgnoreCase(destination.trim()));
                boolean matchesSeats = (capacity == null || bus.getTotalSeats() >= capacity);
                return matchesSource && matchesDest && matchesSeats;
            }
            case TOUR_PACKAGE -> {
                TourPackageInventory tour = (TourPackageInventory) inv;
                return days == null || tour.getDurationDays().equals(days);
            }
            default -> { return true; }
        }
    }
}