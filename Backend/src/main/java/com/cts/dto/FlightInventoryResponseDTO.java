package com.cts.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class FlightInventoryResponseDTO extends BaseInventoryResponseDTO {
    private Integer totalSeats;
    private String flightNumber;
    private String airlineName;
    private String departureAirport;
    private String arrivalAirport;
    private boolean isConnecting;
    private String layoverDetails;
    private String startTime;
    private String endTime;
    private double numberOfHours;
    private List<SeatTierDTO> seatTiers;
}
