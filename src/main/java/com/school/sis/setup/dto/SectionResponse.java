package com.school.sis.setup.dto;

import com.school.sis.setup.entity.ActiveStatus;

import java.util.UUID;

public record SectionResponse(
        UUID id,
        String sectionCode,
        UUID programId,
        String programCode,
        UUID schoolYearId,
        String schoolYear,
        UUID semesterId,
        String semesterName,
        int yearLevel,
        ActiveStatus status
) {
}
