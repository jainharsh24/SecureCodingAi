package com.securecode.ai.dto;

public record AuthenticationResponse(
        String message,
        String email,
        String role
) {
}
