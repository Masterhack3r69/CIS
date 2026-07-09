package com.school.sis.setup.dto;

import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.entity.DegreeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record ProgramRequest(
        @NotBlank String programCode,
        @NotBlank String programName,
        @NotNull UUID departmentId,
        @NotNull DegreeType degreeType,
        Integer programDuration,
        String description,
        ActiveStatus status
) {
}
