package com.school.sis.fee.dto;

import com.school.sis.fee.entity.AssessmentStatus;

import java.util.UUID;

public record AssessmentSearchCriteria(
        String search,
        UUID studentId,
        UUID enrollmentId,
        UUID schoolYearId,
        UUID semesterId,
        AssessmentStatus status
) {
}
