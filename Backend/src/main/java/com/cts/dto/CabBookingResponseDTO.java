package com.cts.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class CabBookingResponseDTO extends BaseBookingResponseDTO {
    private LocalDate targetTravelDate;
    private String pickupLocation;
    private String dropoffLocation;
}
