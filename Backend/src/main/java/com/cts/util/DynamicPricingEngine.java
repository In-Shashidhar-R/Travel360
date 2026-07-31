package com.cts.util;

import com.cts.entity.BusInventory;
import com.cts.entity.CabInventory;
import com.cts.entity.FlightInventory;
import com.cts.entity.HotelInventory;
import com.cts.entity.Inventory;
import com.cts.entity.TourPackageInventory;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;


@Component
public class DynamicPricingEngine {

    // Weekend / lead-time
    private static final double WEEKEND_MARKUP = 1.15;
    private static final double EARLY_BIRD_DISCOUNT = 0.90;
    private static final double NEXT_WEEK_MARKUP = 1.20;
    private static final double LAST_MINUTE_MARKUP = 1.40;
    private static final double HOTEL_LIQUIDATION_DISCOUNT = 0.75;
    private static final long EARLY_BIRD_THRESHOLD_DAYS = 60;
    private static final long HOTEL_LIQUIDATION_DAYS = 2;

    // Cab rush-hour
    private static final double CAB_RUSH_HOUR_SURGE = 1.35;
    private static final double CAB_LATE_NIGHT_SURGE = 1.25;

    // Capacity-exhaustion surge tiers
    private static final double SURGE_CRITICAL = 1.75;
    private static final double SURGE_HIGH = 1.40;
    private static final double SURGE_MODERATE = 1.15;
    private static final double OCCUPANCY_CRITICAL = 90.0;
    private static final double OCCUPANCY_HIGH = 75.0;
    private static final double OCCUPANCY_MODERATE = 50.0;

    public double calculateDynamicUnitPrice(Inventory inv, LocalDate travelDate, int liveFilledSeats) {
        double price = inv.getBasePricePerUnit();
        LocalDate now = LocalDate.now();

        if (travelDate != null && isWeekend(travelDate.getDayOfWeek())) {
            price *= WEEKEND_MARKUP;
        }

        if (travelDate != null) {
            long daysUntilTravel = ChronoUnit.DAYS.between(now, travelDate);
            if (inv instanceof HotelInventory) {
                if (daysUntilTravel >= 0 && daysUntilTravel <= HOTEL_LIQUIDATION_DAYS) {
                    return price * HOTEL_LIQUIDATION_DISCOUNT;
                }
            } else if (inv instanceof FlightInventory || inv instanceof BusInventory || inv instanceof TourPackageInventory) {
                if (daysUntilTravel > EARLY_BIRD_THRESHOLD_DAYS) {
                    price *= EARLY_BIRD_DISCOUNT;
                } else if (daysUntilTravel <= 7 && daysUntilTravel >= 3) {
                    price *= NEXT_WEEK_MARKUP;
                } else if (daysUntilTravel < 3 && daysUntilTravel >= 0) {
                    price *= LAST_MINUTE_MARKUP;
                }
            }
        }

        if (inv instanceof CabInventory) {
            int hour = LocalTime.now().getHour();
            if ((hour >= 8 && hour < 11) || (hour >= 17 && hour < 20)) {
                price *= CAB_RUSH_HOUR_SURGE;
            } else if (hour >= 23 || hour < 4) {
                price *= CAB_LATE_NIGHT_SURGE;
            }
        }

        int totalCapacity = getInventoryTotalCapacity(inv);
        if (totalCapacity > 0 && !(inv instanceof CabInventory)) {
            double occupancyPct = ((double) liveFilledSeats / totalCapacity) * 100;
            if (occupancyPct >= OCCUPANCY_CRITICAL) {
                price *= SURGE_CRITICAL;
            } else if (occupancyPct >= OCCUPANCY_HIGH) {
                price *= SURGE_HIGH;
            } else if (occupancyPct >= OCCUPANCY_MODERATE) {
                price *= SURGE_MODERATE;
            }
        }

        return price;
    }

    private boolean isWeekend(DayOfWeek day) {
        return day == DayOfWeek.FRIDAY || day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY;
    }

    private int getInventoryTotalCapacity(Inventory inv) {
        if (inv instanceof FlightInventory flight) {
            return flight.getTotalSeats();
        }
        if (inv instanceof BusInventory bus) {
            return bus.getTotalSeats();
        }
        if (inv instanceof CabInventory cab) {
            return cab.getSeaterCount();
        }
        if (inv instanceof HotelInventory hotel) {
            return hotel.getTotalSeats();
        }
        return 0;
    }
}
