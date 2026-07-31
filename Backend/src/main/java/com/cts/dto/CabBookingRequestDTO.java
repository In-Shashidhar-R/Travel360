package com.cts.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class CabBookingRequestDTO {
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    @NotNull(message = "Cab Inventory ID is required")
    private Long inventoryId;
    @NotNull(message = "Target rental execution date is required")
    @FutureOrPresent(message = "Target date must not be in the past")
    private LocalDate targetTravelDate;
    @NotBlank(message = "Pickup location address is required")
    private String pickupLocation;
    @NotBlank(message = "Dropoff destination address is required")
    private String dropoffLocation;
    @NotNull(message = "Rider profile reference is required")
    private List<Long> passengerProfileIds;
    
    @NotBlank(message = "Target district filter metric is mandatory")
    private String district;

    @NotBlank(message = "Target state filter metric is mandatory")
    private String state;
    
}