package com.school.sis.grade.dto;

import java.util.UUID;

public record ClassGradeRowResponse(
        UUID enrollmentSubjectId,
        UUID studentId,
        String studentNumber,
        String studentName,
        GradeResponse grade
) {
}
