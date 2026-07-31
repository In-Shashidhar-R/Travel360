package com.cts.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class ItineraryEntryDTO {
    private Long bookingId;
    private String inventoryType;   
    private String status;          
    private LocalDate travelDate;   
    private LocalDate endDate;      
    private String routeOrLocation; 
    private Integer travellers;     
    private Double totalAmount;
}
