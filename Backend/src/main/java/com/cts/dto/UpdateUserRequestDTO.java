package com.cts.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import java.time.LocalDate;

@Data
public class UpdateUserRequestDTO {
    @NotBlank(message = "Name cannot be empty")
    private String name;

    @NotBlank(message = "Phone number cannot be empty")
    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Invalid phone number format")
    private String phone;

    private String address;
    private String city;
    private String state;
    private String country;
    private String gender;
    private LocalDate dateOfBirth;

    // Specialized Travel Agent information (Optional based on Role)
    private String agentBio;
    private Integer agentExperienceYears;
}