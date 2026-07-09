package com.school.sis.enrollment.dto;

import java.util.UUID;

public record EnrollmentValidationIssueResponse(
        String code,
        String message,
        UUID subjectId,
        UUID scheduleId
) {
}
