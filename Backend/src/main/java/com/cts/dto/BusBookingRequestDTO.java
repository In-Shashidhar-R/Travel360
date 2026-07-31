package com.cts.dto;

import com.cts.enumeration.SeatType;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class BusBookingRequestDTO {
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long customerId;
    
    @NotNull(message = "Bus Inventory ID is required")
    private Long inventoryId;
    
    @NotNull(message = "Target travel execution date is required")
    @FutureOrPresent(message = "Target date must not be in the past")
    private LocalDate targetTravelDate;
    
    private Integer requestedSeats;
    
    @NotBlank(message = "Pickup location boarding terminal is mandatory")
    private String pickupLocation;
    
    @NotBlank(message = "Dropoff location destination terminal is mandatory")
    private String dropoffLocation;
    
    @NotNull(message = "A valid chosen seat category tier is required (e.g., AC_SLEEPER, NON_AC_SEATER)")
    private SeatType chosenSeatType; 
    
    @NotEmpty(message = "Passenger profiles list cannot be empty") 
    private List<Long> passengerProfileIds;
}