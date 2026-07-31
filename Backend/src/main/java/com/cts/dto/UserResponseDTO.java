package com.cts.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDTO {
    private Long userId;
    private String name;
    private String email;
    private String role;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String country;
    private LocalDate dateOfBirth;
    private String gender;
    private String agentBio;
    private Integer agentExperienceYears;
}
