package com.school.sis.setup.dto;

import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.entity.DegreeType;

import java.util.UUID;

public record ProgramResponse(
        UUID id,
        String programCode,
        String programName,
        UUID departmentId,
        String departmentCode,
        DegreeType degreeType,
        Integer programDuration,
        String description,
        ActiveStatus status
) {
}
