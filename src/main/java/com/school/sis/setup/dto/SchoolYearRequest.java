package com.school.sis.setup.dto;

import jakarta.validation.constraints.NotBlank;

public record SchoolYearRequest(
        @NotBlank String schoolYear,
        boolean active
) {
}
