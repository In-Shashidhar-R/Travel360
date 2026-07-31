package com.cts.util;

import com.cts.entity.FlightInventory;
import com.cts.entity.HotelInventory;
import com.cts.entity.Inventory;
import com.cts.enumeration.InventoryType;
import com.cts.enumeration.Status;
import com.cts.exception.InventoryInUseException;
import com.cts.exception.InventoryTypeMismatchException;
import com.cts.exception.ResourceNotFoundException;
import com.cts.repository.BookingRepository;
import com.cts.repository.InventoryRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryManagementUtilTest {

    @Mock private InventoryRepository inventoryRepository;
    @Mock private BookingRepository bookingRepository;
    @Mock private DynamicPricingEngine pricingEngine;

    @InjectMocks
    private InventoryManagementUtil util;

    private FlightInventory flight() {
        return FlightInventory.builder()
                .flightNumber("AI-1").airlineName("TestAir")
                .departureAirport("MAA").arrivalAirport("DEL")
                .isConnecting(false).startTime("10:00").endTime("12:00").numberOfHours(2)
                .seatTiers(List.of()).inventoryId(1L)
                .itemType(InventoryType.FLIGHT).basePricePerUnit(1000.0).status(Status.ACTIVE).build();
    }

    private HotelInventory hotel() {
        return HotelInventory.builder()
                .hotelName("H").roomType("D").totalSeats(50).starRating(4)
                .inventoryId(1L).itemType(InventoryType.HOTEL)
                .basePricePerUnit(2000.0).status(Status.ACTIVE).build();
    }

    @Test
    void fetchInventory_notFound_throwsResourceNotFound() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> util.fetchInventory(1L));
    }

    @Test
    void fetchInventoryOfType_wrongType_throwsTypeMismatch() {
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(hotel()));
        assertThrows(InventoryTypeMismatchException.class,
                () -> util.fetchInventoryOfType(1L, InventoryType.FLIGHT));
    }

    @Test
    void deleteInventoryOfType_withBookings_throwsInUseAndDoesNotDelete() {
        FlightInventory f = flight();
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(f));
        when(bookingRepository.countByInventory(f)).thenReturn(3L);

        assertThrows(InventoryInUseException.class,
                () -> util.deleteInventoryOfType(1L, InventoryType.FLIGHT));
        verify(inventoryRepository, never()).delete(any());
    }

    @Test
    void deleteInventoryOfType_noBookings_deletes() {
        FlightInventory f = flight();
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(f));
        when(bookingRepository.countByInventory(f)).thenReturn(0L);

        util.deleteInventoryOfType(1L, InventoryType.FLIGHT);
        verify(inventoryRepository, times(1)).delete(f);
    }

    @Test
    void updateStatus_setsStatusAndSaves() {
        FlightInventory f = flight();
        when(inventoryRepository.findById(1L)).thenReturn(Optional.of(f));
        when(inventoryRepository.save(any(Inventory.class))).thenAnswer(a -> a.getArgument(0));

        Inventory result = util.updateStatus(1L, Status.INACTIVE);
        assertEquals(Status.INACTIVE, result.getStatus());
        verify(inventoryRepository, times(1)).save(f);
    }
}
