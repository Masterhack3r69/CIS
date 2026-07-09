package com.school.sis.auth.dto;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        long expiresInSeconds,
        UserSummary user
) {
}
