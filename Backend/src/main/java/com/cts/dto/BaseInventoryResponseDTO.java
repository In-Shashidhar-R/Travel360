package com.cts.dto;

import lombok.Data;

@Data
public abstract class BaseInventoryResponseDTO {
    private Long inventoryId;
    private Long partnerId;
    private String partnerName;
    private String itemType;
    private double basePricePerSeat;
    private String status;
}