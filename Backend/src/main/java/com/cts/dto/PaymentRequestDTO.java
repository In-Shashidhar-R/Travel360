package com.cts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentRequestDTO {
    @NotNull(message = "Target invoice matching linkage record indicator parameter is mandatory")
    private Long invoiceId;

    @NotBlank(message = "Financial collection transaction clearing variant type channel name must be declared")
    private String method;
}