package com.cts.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private Long userId;
    private String name;
    private String email;
    private String role;
    private String token;
	
}
