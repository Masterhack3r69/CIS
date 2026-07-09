package com.school.sis.auth.dto;

import java.util.List;
import java.util.UUID;

public record UserSummary(
        UUID id,
        String username,
        String email,
        String fullName,
        List<String> roles,
        List<String> permissions
) {
}
