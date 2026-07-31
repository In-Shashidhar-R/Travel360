package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InvoiceResponseDTO {
    
    private Long invoiceId;         
    private Long bookingId; 
    private String customerName;   
    private double amount;         
    private LocalDate generatedDate; 
    private String status;         
}