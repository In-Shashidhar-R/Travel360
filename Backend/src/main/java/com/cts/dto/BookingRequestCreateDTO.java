package com.cts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BookingRequestCreateDTO {

    @NotNull(message = "Inventory ID of the target package is required")
    private Long inventoryId;

    @NotBlank(message = "Please describe your modification / requirements")
    private String customerRequirements;
}
