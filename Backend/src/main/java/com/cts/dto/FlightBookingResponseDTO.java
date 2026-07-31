package com.cts.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class FlightBookingResponseDTO extends BaseBookingResponseDTO {
    private Integer requestedSeats;
    private LocalDate targetTravelDate;
    private String pickupLocation;
    private String dropoffLocation;
    private String chosenSeatType;
}
