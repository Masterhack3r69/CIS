package com.school.sis.student.dto;

import com.school.sis.student.entity.DocumentVerificationStatus;
import jakarta.validation.constraints.NotNull;

public record DocumentVerificationRequest(
        @NotNull DocumentVerificationStatus status,
        String remarks
) {
}
