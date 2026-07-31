package com.cts.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class UserRequestDTO {
    @NotBlank(message = "Name field cannot be left blank")
    private String name;

    @NotBlank(message = "Email field cannot be left blank")
    @Email(message = "Invalid format structure for email address mapping")
    private String email;

    @NotBlank(message = "Password field cannot be left blank")
    @Size(min = 4, message = "Password must span 4 or more characters")
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

    @Past(message = "Date of birth must be in the past")
    private LocalDate dateOfBirth;

    @Pattern(regexp = "(?i)^(MALE|FEMALE|OTHER|PREFER_NOT_TO_SAY)?$",
            message = "Gender must be MALE, FEMALE, OTHER, or PREFER_NOT_TO_SAY")
    private String gender;
    
}
