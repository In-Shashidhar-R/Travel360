package com.cts.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ForgotPasswordRequestDTO {
    @NotBlank(message = "Account recovery requires an email address mapping anchor.")
    @Email(message = "Invalid email format string configuration.")
    private String email;
}