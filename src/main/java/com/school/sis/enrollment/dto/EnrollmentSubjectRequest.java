package com.school.sis.enrollment.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record EnrollmentSubjectRequest(
        @NotNull UUID scheduleId
) {
}
