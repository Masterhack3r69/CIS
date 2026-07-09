package com.school.sis.enrollment.dto;

import java.util.UUID;

public record EnrollmentUpdateRequest(
        UUID sectionId,
        String remarks
) {
}
