package com.school.sis.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.Set;
import java.util.UUID;

public record UserRequest(
        @NotBlank @Size(max = 80) String username,
        @NotBlank @Email @Size(max = 120) String email,
        @NotBlank @Size(max = 255) String fullName,
        @Size(min = 8, max = 120) String initialPassword,
        @NotEmpty Set<UUID> roleIds,
        UUID facultyId
) {}
