package com.school.sis.enrollment.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EnrollmentRequest(
        @NotNull UUID studentId,
        @NotNull UUID schoolYearId,
        @NotNull UUID semesterId,
        UUID sectionId,
        String remarks
) {
}
