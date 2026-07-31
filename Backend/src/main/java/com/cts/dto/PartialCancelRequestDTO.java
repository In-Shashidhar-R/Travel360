package com.cts.dto;

import lombok.Data;
import java.util.List;

@Data
public class PartialCancelRequestDTO {
    private Long customerId;

    private List<Long> passengerProfileIdsToCancel;

    private String cancellationRemarks;
}
