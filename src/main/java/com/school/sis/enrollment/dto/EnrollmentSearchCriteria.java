package com.school.sis.enrollment.dto;

import com.school.sis.enrollment.entity.EnrollmentStatus;

import java.util.UUID;

public record EnrollmentSearchCriteria(
        String search,
        UUID studentId,
        UUID programId,
        UUID sectionId,
        UUID schoolYearId,
        UUID semesterId,
        EnrollmentStatus status
) {
}
