package com.school.sis.enrollment.dto;

import java.util.UUID;

public record EnrollmentUpdateRequest(
        Integer yearLevel,
        UUID sectionId,
        String remarks
) {
}
