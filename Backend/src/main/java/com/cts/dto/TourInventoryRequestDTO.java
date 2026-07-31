package com.cts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TourInventoryRequestDTO {
    @NotNull(message = "Partner identifier is required")
    private Long partnerId;

    @NotNull(message = "Base pricing configuration rate per consumer is required")
    private double basePricePerPersonForPackage;
    @NotBlank(message = "Vacation package bundle name is required")
    private String packageName;
    @NotBlank(message = "Chronological full itinerary description breakdown is required")
    private String fullItineraryDetails;
    @NotNull(message = "Total standard duration days metrics count is required")
    private Integer durationDays;
    private Long travelAgentId;
}