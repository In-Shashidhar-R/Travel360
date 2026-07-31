package com.cts.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComplaintResponseDTO {
    private Long complaintId;
    private Long raisedByUserId;
    private String raisedByName;
    private Long relatedBookingId;
    private String subject;
    private String description;
    private String status;
    private String resolutionNote;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
