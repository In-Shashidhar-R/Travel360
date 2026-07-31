package com.cts.dto;

import com.cts.entity.SeatTierCapacity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class FlightInventoryRequestDTO {
    @NotNull(message = "Partner identifier is required")
    private Long partnerId;

    @NotNull(message = "Base price per seat configuration is required")
    private double basePricePerSeat;

    @NotBlank(message = "Flight number code is required")
    private String flightNumber;

    @NotBlank(message = "Airline carrier name is required")
    private String airlineName;

    @NotBlank(message = "Departure airport code is required")
    private String departureAirport;

    @NotBlank(message = "Arrival airport code is required")
    private String arrivalAirport;

    private boolean isConnecting;
    private String layoverDetails;

    @NotBlank(message = "Scheduled departure time (HH:mm:ss) is required")
    private String startTime;

    @NotBlank(message = "Scheduled arrival time (HH:mm:ss) is required")
    private String endTime;

    @NotEmpty(message = "Flight provision requests must include at least one seat tier configuration (e.g., ECONOMY, BUSINESS)")
    @Valid 
    private List<SeatTierCapacity> seatTiers;
}