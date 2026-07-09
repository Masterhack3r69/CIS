package com.school.sis.fee.dto;

import com.school.sis.fee.entity.AssessmentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record AssessmentSummaryResponse(
        UUID id,
        String assessmentNumber,
        UUID enrollmentId,
        UUID studentId,
        String studentNumber,
        String studentName,
        String schoolYear,
        String semesterName,
        AssessmentStatus status,
        BigDecimal totalUnits,
        BigDecimal totalAmount
) {
}
