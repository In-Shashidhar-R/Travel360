package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

import com.cts.enumeration.PaymentType;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentResponseDTO {
    private Long paymentId;
    private Long invoiceId;
    private double amount;
    private LocalDate paymentDate;
    private PaymentType paymentType;
    private String method;
    private String status;
}