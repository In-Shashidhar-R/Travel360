package com.cts.dto;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

import com.cts.enumeration.SeatType;

import io.swagger.v3.oas.annotations.media.Schema;

@Data
public class FlightBookingRequestDTO {
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
    private Long customerId;
    @NotNull(message = "Flight Inventory ID is required")
    private Long inventoryId;
    @NotNull(message = "Target departure date is required")
    @FutureOrPresent(message = "Target date must not be in the past")
    private LocalDate targetTravelDate;
    @NotNull(message = "Seat Cannot be null")
    private SeatType chosenSeatType; 
    @NotNull(message = "Passenger profiles list cannot be empty")
    private List<Long> passengerProfileIds;
}