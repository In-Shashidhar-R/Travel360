package com.cts.dto;

import com.cts.enumeration.BookingRequestStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class BookingRequestResponseDTO {

    private Long requestId;
    private BookingRequestStatus status;

    private Long customerId;
    private String customerName;

    private Long assignedAgentId;
    private String assignedAgentName;
    private String assignedAgentEmail;

    private Long inventoryId;
    private String inventoryItemType;
    private String packageName;

    private String customerRequirements;
    private String agentNotes;

    private LocalDateTime requestedDate;
    private LocalDateTime updatedDate;

    private Long resultingBookingId;
}
