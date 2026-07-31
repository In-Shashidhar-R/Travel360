package com.cts.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class AgentRequestDTO {
    @NotBlank(message = "Agent assignment name is mandatory")
    private String name;

    @NotBlank(message = "Agent monitoring email mapping is mandatory")
    @Email(message = "Improper structure for agent email context mapping")
    private String email;

    @NotBlank(message = "Initial pass phrase parameter configuration is required")
    private String password;

    @NotBlank(message = "Phone number is required")
    @Pattern(
        regexp = "^[+]?[0-9\\-\\s()]{6,20}$",
        message = "Phone must be 7-20 chars: optional + prefix, digits, spaces, dashes, parentheses"
    )
    private String phone;
    private String address;
    private String city;
    private String state;
    private String country;
    private String agentBio;
    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;
    private String gender;

    @Min(value = 0, message = "Experience years cannot be negative")
    private Integer agentExperienceYears;
}
