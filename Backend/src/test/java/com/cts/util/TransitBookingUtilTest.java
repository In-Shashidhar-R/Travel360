package com.cts.util;

import com.cts.entity.CabInventory;
import com.cts.entity.TourPackageInventory;
import com.cts.enumeration.InventoryType;
import com.cts.enumeration.Status;
import com.cts.exception.InvalidTimelineException;
import com.cts.repository.BookingRepository;
import com.cts.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransitBookingUtilTest {

    @Mock
    private InventoryRepository inventoryRepository;
    @Mock
    private BookingRepository bookingRepository;
    @Mock
    private DynamicPricingEngine pricingEngine;

    @InjectMocks
    private TransitBookingUtil transitBookingUtil;

    @Test
    void fetchAndValidateCab_districtMismatch_throwsInvalidTimeline() {
        CabInventory cab = CabInventory.builder()
                .vehicleRegistrationNumber("TN-09-9999")
                .carModel("Sedan")
                .fuelType("Petrol")
                .seaterCount(4)
                .district("Chennai")
                .state("Tamil Nadu")
                .inventoryId(1L)
                .itemType(InventoryType.CAB)
                .basePricePerUnit(500.0)
                .status(Status.ACTIVE)
                .build();
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(cab));

        assertThrows(InvalidTimelineException.class,
                () -> transitBookingUtil.fetchAndValidateCab(1L, "Mumbai", "Maharashtra"));
    }

    @Test
    void calculateTourCost_headcountMismatch_throwsInvalidTimeline() {
        TourPackageInventory tour = TourPackageInventory.builder()
                .packageName("Golden Triangle")
                .fullItineraryDetails("Delhi-Agra-Jaipur")
                .durationDays(5)
                .inventoryId(1L)
                .itemType(InventoryType.TOUR_PACKAGE)
                .basePricePerUnit(3000.0)
                .status(Status.ACTIVE)
                .build();

        assertThrows(InvalidTimelineException.class,
                () -> transitBookingUtil.calculateTourCost(tour, 3, 2, LocalDate.now().plusDays(10)));
    }
}
