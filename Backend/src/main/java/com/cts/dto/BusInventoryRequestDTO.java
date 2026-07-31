package com.cts.dto;

import com.cts.entity.SeatTierCapacity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class BusInventoryRequestDTO {
    @NotNull(message = "Partner identifier is required")
    private Long partnerId;

    @NotNull(message = "Base price per bus seat configuration is required")
    private double basePricePerSeat;

    @NotBlank(message = "Bus vehicle plate number is required")
    private String busNumberPlate;

    @NotBlank(message = "Operator agency brand name is required")
    private String operatorName;

    @NotBlank(message = "Source terminal point is required")
    private String routeFrom;

    @NotBlank(message = "Destination terminal point is required")
    private String routeTo;

    @NotBlank(message = "Departure timing (HH:mm:ss) is required")
    private String startTime;

    @NotBlank(message = "Arrival timing (HH:mm:ss) is required")
    private String endTime;

    private List<BusStopDTO> routeStops;

    @NotEmpty(message = "Bus provision requests must include at least one seat tier configuration (e.g., AC_SLEEPER, NON_AC_SEATER)")
    @Valid 
    private List<SeatTierCapacity> seatTiers;
}