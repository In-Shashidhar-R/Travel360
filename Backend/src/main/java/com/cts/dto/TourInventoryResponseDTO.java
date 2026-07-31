package com.cts.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TourInventoryResponseDTO extends BaseInventoryResponseDTO {
    private String packageName;
    private String fullItineraryDetails;
    private Integer durationDays;
    private Long travelAgentId;
    private String travelAgentName;
    private String travelAgentEmail;
    private Integer availableSlots;
}
