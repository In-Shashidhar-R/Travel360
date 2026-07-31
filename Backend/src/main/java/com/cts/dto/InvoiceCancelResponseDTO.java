package com.cts.dto;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class InvoiceCancelResponseDTO {
    private Long invoiceId;
    private Long bookingId;
    private Long customerId;
    private String customerName;
    private int passengersCancelledCount;
    private double cancellationFeeApplied;
    private double refundAmount;
    private double updatedNewBookingTotal;
    private LocalDate generatedDate;
    private String status; 
}