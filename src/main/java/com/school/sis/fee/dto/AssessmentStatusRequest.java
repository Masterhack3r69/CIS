package com.school.sis.fee.dto;

import com.school.sis.fee.entity.AssessmentStatus;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record AssessmentStatusRequest(
        @NotNull AssessmentStatus status,
        BigDecimal amountPaid
) {
}
