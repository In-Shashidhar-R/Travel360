package com.cts.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String code;
    private String message;

    public static MessageResponse of(String message) {
        return new MessageResponse("SUCCESS", message);
    }

    public static MessageResponse of(String code, String message) {
        return new MessageResponse(code, message);
    }
}