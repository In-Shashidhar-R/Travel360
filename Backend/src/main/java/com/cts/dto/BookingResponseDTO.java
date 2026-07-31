package com.cts.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class BookingResponseDTO extends BaseBookingResponseDTO {
    private Integer requestedSeats;
    private String pickupLocation;
    private String dropoffLocation;
    private Integer numberOfPersons;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private String chosenSeatType;
}
