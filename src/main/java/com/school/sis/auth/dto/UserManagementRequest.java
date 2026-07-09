package com.school.sis.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.Set;
import java.util.UUID;

public record UserManagementRequest(
        @NotBlank String username,
        @NotBlank @Email String email,
        @NotBlank String fullName,
        Boolean active,
        String password,
        Set<UUID> roleIds
) {
}
