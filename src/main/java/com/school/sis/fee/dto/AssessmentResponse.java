package com.school.sis.fee.dto;

import com.school.sis.fee.entity.AssessmentStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record AssessmentResponse(
        UUID id,
        UUID studentId,
        String studentNumber,
        String studentName,
        UUID enrollmentId,
        UUID schoolYearId,
        String schoolYear,
        UUID semesterId,
        String semesterName,
        BigDecimal totalUnits,
        BigDecimal tuitionAmount,
        BigDecimal laboratoryFeeAmount,
        BigDecimal miscellaneousFeeAmount,
        BigDecimal otherFeeAmount,
        BigDecimal discountAmount,
        BigDecimal penaltyAmount,
        BigDecimal totalAssessment,
        BigDecimal amountPaid,
        BigDecimal balance,
        AssessmentStatus status,
        List<AssessmentItemResponse> items
) {
}
