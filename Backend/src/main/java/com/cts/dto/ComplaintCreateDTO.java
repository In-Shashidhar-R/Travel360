package com.cts.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class ComplaintCreateDTO {

    @NotBlank(message = "Complaint subject is required")
    private String subject;

    @NotBlank(message = "Complaint description is required")
    private String description;

    /** Optional booking this complaint relates to. */
    private Long relatedBookingId;
}
