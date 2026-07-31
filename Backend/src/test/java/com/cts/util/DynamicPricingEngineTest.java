package com.cts.util;

import com.cts.entity.FlightInventory;
import com.cts.entity.HotelInventory;
import com.cts.entity.SeatTierCapacity;
import com.cts.enumeration.InventoryType;
import com.cts.enumeration.SeatType;
import com.cts.enumeration.Status;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DynamicPricingEngineTest {

    private final DynamicPricingEngine engine = new DynamicPricingEngine();

    private static final double DELTA = 0.001;

    private boolean isWeekend(LocalDate d) {
        DayOfWeek day = d.getDayOfWeek();
        return day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private LocalDate weekday(int minGap) {
        LocalDate d = LocalDate.now().plusDays(minGap);
        while (isWeekend(d)) {
            d = d.plusDays(1);
        }
        return d;
    }

    private LocalDate saturday(int minGap) {
        LocalDate d = LocalDate.now().plusDays(minGap);
        while (d.getDayOfWeek() != DayOfWeek.SATURDAY) {
            d = d.plusDays(1);
        }
        return d;
    }

    private FlightInventory flight(double basePrice, int seatsAllocated) {
        SeatTierCapacity tier = SeatTierCapacity.builder()
                .seatType(SeatType.ECONOMY)
                .totalSeatsAllocated(seatsAllocated)
                .priceMultiplier(1.0)
                .build();
        return FlightInventory.builder()
                .flightNumber("AI-101")
                .airlineName("TestAir")
                .departureAirport("MAA")
                .arrivalAirport("DEL")
                .isConnecting(false)
                .startTime("10:00")
                .endTime("12:00")
                .numberOfHours(2)
                .seatTiers(List.of(tier))
                .itemType(InventoryType.FLIGHT)
                .basePricePerUnit(basePrice)
                .status(Status.ACTIVE)
                .build();
    }

    private HotelInventory hotel(double basePrice, int totalRooms) {
        return HotelInventory.builder()
                .hotelName("TestHotel")
                .roomType("Deluxe")
                .totalSeats(totalRooms)
                .starRating(4)
                .itemType(InventoryType.HOTEL)
                .basePricePerUnit(basePrice)
                .status(Status.ACTIVE)
                .build();
    }

    // --- tests ---------------------------------------------------------------

    @Test
    @DisplayName("Weekend travel date applies the 1.15 markup")
    void weekendMarkupApplied() {
        HotelInventory h = hotel(2000.0, 100);
        LocalDate weekend = saturday(3); // > 2 days out, so no liquidation discount
        double price = engine.calculateDynamicUnitPrice(h, weekend, 0);
        assertEquals(2000.0 * 1.15, price, DELTA);
    }

    @Test
    @DisplayName("Hotel within 2 days of check-in receives the 0.75 liquidation discount")
    void hotelLiquidationDiscount() {
        HotelInventory h = hotel(2000.0, 100);
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        double weekendFactor = isWeekend(tomorrow) ? 1.15 : 1.0;
        double price = engine.calculateDynamicUnitPrice(h, tomorrow, 0);
        assertEquals(2000.0 * weekendFactor * 0.75, price, DELTA);
    }

    @Test
    @DisplayName("Flight more than 60 days out receives the 0.90 early-bird discount")
    void flightEarlyBirdDiscount() {
        FlightInventory f = flight(1000.0, 100);
        LocalDate farOut = weekday(63); // weekday, > 60 days
        double price = engine.calculateDynamicUnitPrice(f, farOut, 0);
        assertEquals(1000.0 * 0.90, price, DELTA);
    }

    @Test
    @DisplayName("Critical occupancy (>=90%) applies the 1.75 surge")
    void flightCapacitySurgeCritical() {
        FlightInventory f = flight(1000.0, 100);
        LocalDate midRange = weekday(30); // weekday, 8..60 days => no lead-time curve
        double price = engine.calculateDynamicUnitPrice(f, midRange, 90); // 90% full
        assertEquals(1000.0 * 1.75, price, DELTA);
    }

    @Test
    @DisplayName("Moderate occupancy (>=50%) applies the 1.15 surge")
    void flightCapacitySurgeModerate() {
        FlightInventory f = flight(1000.0, 100);
        LocalDate midRange = weekday(30);
        double price = engine.calculateDynamicUnitPrice(f, midRange, 50); // 50% full
        assertEquals(1000.0 * 1.15, price, DELTA);
    }

    @Test
    @DisplayName("Low occupancy in the mid lead-time window leaves the base price unchanged")
    void flightNoAdjustmentMidRange() {
        FlightInventory f = flight(1000.0, 100);
        LocalDate midRange = weekday(30);
        double price = engine.calculateDynamicUnitPrice(f, midRange, 10); // 10% full
        assertEquals(1000.0, price, DELTA);
    }
}
