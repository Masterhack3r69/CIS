package com.school.sis.fee.dto;

import com.school.sis.fee.entity.FeeCategory;
import com.school.sis.fee.entity.FeeComputationType;

import java.math.BigDecimal;
import java.util.UUID;

public record AssessmentItemResponse(
        UUID id,
        UUID feeItemId,
        String feeCode,
        UUID enrollmentSubjectId,
        String description,
        FeeCategory category,
        FeeComputationType computationType,
        BigDecimal quantity,
        BigDecimal unitAmount,
        BigDecimal totalAmount
) {
}
