package com.school.sis.setup.dto;

import com.school.sis.setup.entity.ActiveStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record SectionRequest(
        @NotBlank String sectionCode,
        @NotNull UUID programId,
        @NotNull UUID schoolYearId,
        @NotNull UUID semesterId,
        @Min(1) int yearLevel,
        ActiveStatus status
) {
}
