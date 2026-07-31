package com.cts.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HotelInventoryRequestDTO {
    @NotNull(message = "Partner identifier is required")
    private Long partnerId;
    @NotNull(message = "Total rooms capacity allocation is required")
    @Min(value = 1, message = "Rooms must be at least 1")
    private Integer totalRooms;
    @NotNull(message = "Base price per night allocation is required")
    private double basePricePerRoom;
    @NotBlank(message = "Hotel name is required")
    private String hotelName;
    @NotBlank(message = "Room variant classification type is required")
    private String roomType;
    @Min(value = 1) @Max(value = 5)
    private Integer hotelRating;
    @NotBlank(message = "Physical address location is required")
    private String addressLocation;

    @NotBlank(message = "District is required")
    private String district;

    @NotBlank(message = "State is required")
    private String state;

    @NotBlank(message = "Country is required")
    private String country;
}