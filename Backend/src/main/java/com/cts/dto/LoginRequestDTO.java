package com.cts.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequestDTO {
    @NotBlank(message = "Authentication requires an account email address.")
    @Email(message = "Invalid email format string configuration.")
    private String email;

    @NotBlank(message = "Authentication requires an account security password.")
    private String password;
}