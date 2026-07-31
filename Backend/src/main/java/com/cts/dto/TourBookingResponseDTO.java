package com.cts.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.time.LocalDate;

@Data
@EqualsAndHashCode(callSuper = true)
public class TourBookingResponseDTO extends BaseBookingResponseDTO {
    private Integer numberOfPersons;
    private LocalDate targetTravelDate;
}
