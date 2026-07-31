package com.cts.service;

import com.cts.dto.*;
import com.cts.enumeration.InventoryType;

import java.time.LocalDate;
import java.util.List;

public interface InventoryService {

    FlightInventoryResponseDTO provisionFlight(FlightInventoryRequestDTO request);
    HotelInventoryResponseDTO provisionHotel(HotelInventoryRequestDTO request);
    BusInventoryResponseDTO provisionBus(BusInventoryRequestDTO request);
    CabInventoryResponseDTO provisionCab(CabInventoryRequestDTO request);
    TourInventoryResponseDTO provisionTour(TourInventoryRequestDTO request);

    FlightInventoryResponseDTO updateFlight(Long inventoryId, FlightInventoryRequestDTO request);
    HotelInventoryResponseDTO updateHotel(Long inventoryId, HotelInventoryRequestDTO request);
    BusInventoryResponseDTO updateBus(Long inventoryId, BusInventoryRequestDTO request);
    CabInventoryResponseDTO updateCab(Long inventoryId, CabInventoryRequestDTO request);
    TourInventoryResponseDTO updateTour(Long inventoryId, TourInventoryRequestDTO request);

    void deleteInventory(Long inventoryId, InventoryType expectedType);
    Object deactivateInventory(Long inventoryId);
    Object activateInventory(Long inventoryId);

    Object getInventoryById(Long inventoryId, LocalDate targetDate);
    List<Object> searchByRoute(String source, String destination, LocalDate targetDate);
    List<Object> getAllInventories(LocalDate targetDate);
    List<Object> filterInventories(InventoryType itemType, String state, String district, String city,
                                   String source, String destination, Integer requiredCapacity,
                                   Integer durationDays, Double maxPrice, LocalDate targetDate);

    PageResponse<Object> getMyInventories(org.springframework.data.domain.Pageable pageable);
}
