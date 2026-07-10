package com.school.sis.fee.dto;

import com.school.sis.fee.entity.AssessmentStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record AssessmentSummaryResponse(
        UUID id,
        UUID studentId,
        String studentNumber,
        String studentName,
        UUID enrollmentId,
        String schoolYear,
        String semesterName,
        BigDecimal totalAssessment,
        BigDecimal amountPaid,
        BigDecimal balance,
        AssessmentStatus status
) {
}
