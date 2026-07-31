package com.cts.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PassengerProfileDTO {
	@Schema(accessMode = Schema.AccessMode.READ_ONLY)
	private Long profileId;

    @NotBlank(message = "Passenger name is mandatory")
    private String name;

    @NotNull(message = "Age parameters required")
    @Min(value = 1, message = "Age must be valid")
    private Integer age;

    @NotBlank(message = "Gender designation required")
    private String gender;

    @NotBlank(message = "Identity proof format declaration required (PAN, AADHAAR, PASSPORT, DRIVING_LICENSE)")
    private String idProofType;

    @NotBlank(message = "Identity document key unique number required")
    private String idProofNumber;
}