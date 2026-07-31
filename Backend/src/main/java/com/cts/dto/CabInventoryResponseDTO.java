package com.cts.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class CabInventoryResponseDTO extends BaseInventoryResponseDTO {
    private String vehicleRegistrationNumber;
    private String carModel;
    private String fuelType;
    private Integer seaterCount;
    private String district;
    private String state;
    private Integer availableSeats;
}
