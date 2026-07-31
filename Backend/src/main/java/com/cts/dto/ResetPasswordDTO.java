package com.cts.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ResetPasswordDTO {
    @NotBlank(message = "Account context recovery verification target email is required.")
    @Email(message = "Invalid email format string configuration.")
    private String email;

    @NotBlank(message = "New target account security credential replacement is mandatory.")
    private String newPassword;
}