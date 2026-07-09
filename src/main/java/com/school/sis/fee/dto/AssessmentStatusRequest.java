package com.school.sis.fee.dto;

import com.school.sis.fee.entity.AssessmentStatus;
import jakarta.validation.constraints.NotNull;

public record AssessmentStatusRequest(
        @NotNull AssessmentStatus status,
        String remarks
) {
}
