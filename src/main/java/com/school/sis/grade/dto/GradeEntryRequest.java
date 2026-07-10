package com.school.sis.grade.dto;

import com.school.sis.grade.entity.GradeRemark;

import java.math.BigDecimal;
import java.util.UUID;

public record GradeEntryRequest(
        UUID enrollmentSubjectId,
        BigDecimal finalGrade,
        GradeRemark remarks
) {
}
