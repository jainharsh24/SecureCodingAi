package com.securecode.ai.dto;

public record AuthenticationResponse(
        String message,
        String name,
        String email,
        String role,
        String token
) {
}
