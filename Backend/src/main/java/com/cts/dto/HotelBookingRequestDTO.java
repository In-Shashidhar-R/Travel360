package com.cts.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class HotelBookingRequestDTO {
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    @NotNull(message = "Hotel Inventory ID is required")
    private Long inventoryId;
    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Target date must not be in the past")
    private LocalDate checkInDate;
    @NotNull(message = "Check-out date is required")
    @FutureOrPresent(message = "Target date must not be in the past")
    private LocalDate checkOutDate;
    private Integer requestedRooms; 
    @NotNull(message = "Guest passenger profiles list is required")
    private List<Long> passengerProfileIds;
}