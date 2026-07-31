package com.cts.dto;

import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public abstract class BaseBookingResponseDTO {
    private Long bookingId;
    private Long customerId;
    private String customerName;
    private Long partnerId;
    private String partnerName;
    private Long inventoryId;
    private String itemType;
    private LocalDate bookingDate;
    private String status;
    private double totalAmount;
    private List<PassengerSnapshotDTO> passengers;
}
