package com.cts.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class TourBookingRequestDTO {
    @NotNull(message = "Customer ID is required")
    private Long customerId;
    @NotNull(message = "Tour Package Inventory ID is required")
    private Long inventoryId;
    @NotNull(message = "Target tour commencement date is required")
    @Future(message = "Target date must not be in the past")
    private LocalDate targetTravelDate;
    @NotNull(message = "Total number of persons joining is mandatory")
    @Min(value = 1, message = "Tour headcount allocation must be at least 1")
    private Integer numberOfPersons;
    @NotNull(message = "Tour member passenger profiles list is required")
    private List<Long> passengerProfileIds;
}