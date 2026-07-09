package com.school.sis.fee.dto;

import com.school.sis.fee.entity.AssessmentStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record AssessmentResponse(
        UUID id,
        String assessmentNumber,
        UUID enrollmentId,
        UUID studentId,
        String studentNumber,
        String studentName,
        UUID schoolYearId,
        String schoolYear,
        UUID semesterId,
        String semesterName,
        AssessmentStatus status,
        BigDecimal totalUnits,
        BigDecimal subtotalAmount,
        BigDecimal discountAmount,
        BigDecimal totalAmount,
        String remarks,
        Instant generatedAt,
        List<AssessmentItemResponse> items
) {
}
