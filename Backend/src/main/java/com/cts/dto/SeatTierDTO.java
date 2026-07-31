package com.cts.dto;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class SeatTierDTO {
	
	private int availableSeats;
	
    private String seatType;

    private double pricePerSeat;

    private double priceMultiplier;

    private int totalSeatsAllocated;
}
