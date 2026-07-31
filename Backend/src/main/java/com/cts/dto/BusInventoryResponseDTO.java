package com.cts.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class BusInventoryResponseDTO extends BaseInventoryResponseDTO {
    private Integer totalSeats;
    private String busNumberPlate;
    private String operatorName;
    private String routeFrom;
    private String routeTo;
    private String startTime;
    private String endTime;
    private double numberOfHours;
    private List<BusStopDTO> routeStops;
    private List<SeatTierDTO> seatTiers;
}
