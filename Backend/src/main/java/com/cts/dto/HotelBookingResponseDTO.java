package com.cts.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class HotelBookingResponseDTO extends BaseBookingResponseDTO {
    private Integer requestedRooms;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
}
