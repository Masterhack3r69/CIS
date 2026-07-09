package com.school.sis.setup.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record SemesterRequest(
        @NotBlank String name,
        @Min(1) int sortOrder,
        boolean active
) {
}
