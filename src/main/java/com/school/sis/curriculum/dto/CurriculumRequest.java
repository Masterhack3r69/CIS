package com.school.sis.curriculum.dto;

import com.school.sis.curriculum.entity.CurriculumStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CurriculumRequest(
        @NotNull UUID programId,
        @NotBlank String curriculumCode,
        @NotBlank String curriculumName,
        @NotBlank String effectiveSchoolYear,
        @NotBlank String version,
        @NotNull CurriculumStatus status,
        String description
) {
}
