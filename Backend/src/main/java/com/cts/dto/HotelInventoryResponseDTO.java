package com.cts.dto;

import lombok.Data;

@Data
public class HotelInventoryResponseDTO {
    private Long inventoryId;
    private Long partnerId;
    private String partnerName;
    private String itemType;
    private Integer availableRooms;
    private Integer totalRooms; 
    private double basePricePerSeat; 
    private String status;
    private String hotelName;
    private String roomType;
    private Integer hotelRating;
    private String addressLocation;
    private String district;
    private String state;
    private String country;
}