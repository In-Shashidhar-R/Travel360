package com.cts.dto;

import java.time.LocalDate;

import com.cts.enumeration.InventoryType;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PartnerRequestDTO {
    @NotBlank(message = "Partner business name is mandatory")
    private String name;

    @NotNull(message = "Partner type is mandatory (FLIGHT / HOTEL / BUS / CAB / TOUR_PACKAGE)")
    private InventoryType type;

    @NotBlank(message = "Partner contact email cannot be blank")
    @Email(message = "Invalid partner email format structure")
    private String email;

    @NotBlank(message = "Partner contact number cannot be blank")
    @Pattern(regexp = "^[+]?[0-9\\-\\s()]{6,20}$",
            message = "Contact number must be 7-20 chars: optional + prefix, digits, spaces, dashes, parentheses")
    private String contactNumber;

    @NotBlank(message = "Partner login password is mandatory")
    @Size(min = 5, message = "Password must be at least 5 characters")
    @Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)(?=.*[^A-Za-z0-9]).{5,}$",
        message = "Password must include at least one letter, one digit, and one special character"
    )
    private String password;

    private String address;
    private String city;
    private String state;
    private String country;
    private String gender;
    
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "^$|^[0-9A-Z]{10,20}$",
            message = "GST number must be 10-20 uppercase alphanumeric characters")
    private String gstNumber;

    @jakarta.validation.constraints.DecimalMin(value = "0.0", message = "Commission rate cannot be negative")
    @jakarta.validation.constraints.DecimalMax(value = "100.0", message = "Commission rate cannot exceed 100%")
    private Double commissionRate;
}
