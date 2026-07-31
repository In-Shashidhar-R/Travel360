package com.cts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CabInventoryRequestDTO {
    @NotNull(message = "Partner identifier is required")
    private Long partnerId;
    @NotNull(message = "Base price per standard kilometer or flat transfer is required")
    private double basePricePerSeat;
    @NotBlank(message = "Vehicle registration plate number is required")
    private String vehicleRegistrationNumber;
    @NotBlank(message = "Car manufacture model name is required")
    private String carModel;
    @NotBlank(message = "Fuel variety specification (PETROL/DIESEL) is required")
    private String fuelType;
    @NotNull(message = "Maximum passenger occupancy seater count is required")
    private Integer seaterCount;
    
    @NotBlank(message = "Operating district location is mandatory")
    private String district;

    @NotBlank(message = "Operating state location is mandatory")
    private String state;
}