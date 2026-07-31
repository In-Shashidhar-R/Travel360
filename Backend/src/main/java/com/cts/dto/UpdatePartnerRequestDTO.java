package com.cts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDate; // <-- Missing Import

import com.cts.enumeration.InventoryType;

@Data
public class UpdatePartnerRequestDTO {
    @NotBlank(message = "Partner business name cannot be blank")
    private String name;

    @NotNull(message = "Partner merchant category type is required")
    private InventoryType type;

    @NotBlank(message = "Contact number is mandatory")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid contact phone number format")
    private String contactNumber;

    private String address;
    private String city;
    private String state;
    private String country;
    private String gender;
    
    private LocalDate dateOfBirth;

    @NotBlank(message = "GST identification registration number is required")
    private String gstNumber;

    @NotNull(message = "Corporate commission rate calculation factor must be specified")
    private Double commissionRate;
}