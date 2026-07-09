package com.school.sis.grade.dto;

import com.school.sis.grade.entity.SpecialGrade;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record GradeEncodeItemRequest(
        @NotNull UUID enrollmentSubjectId,
        BigDecimal finalGrade,
        SpecialGrade specialGrade,
        String notes
) {
}
