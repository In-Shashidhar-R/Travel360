package com.cts.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;


@Data
public class ComplaintResolveDTO {

    @NotBlank(message = "Resolution note is required")
    private String resolutionNote;
}
