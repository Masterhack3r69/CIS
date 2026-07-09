package com.school.sis.curriculum.dto;

import com.school.sis.curriculum.entity.CurriculumStatus;

import java.util.UUID;

public record CurriculumResponse(
        UUID id,
        UUID programId,
        String programCode,
        String curriculumCode,
        String curriculumName,
        String effectiveSchoolYear,
        String version,
        CurriculumStatus status,
        String description
) {
}
