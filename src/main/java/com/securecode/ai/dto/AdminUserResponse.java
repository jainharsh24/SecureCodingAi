package com.securecode.ai.dto;

import java.time.Instant;

/** Minimal administrative representation; password hashes are never exposed. */
public record AdminUserResponse(Long id, String name, String email, String role, Instant createdAt) { }
