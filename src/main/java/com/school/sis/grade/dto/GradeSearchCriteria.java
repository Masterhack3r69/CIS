package com.school.sis.grade.dto;

import com.school.sis.grade.entity.GradeStatus;

import java.util.UUID;

public record GradeSearchCriteria(
        String search,
        UUID studentId,
        UUID courseId,
        UUID sectionId,
        UUID facultyId,
        UUID schoolYearId,
        UUID semesterId,
        GradeStatus status
) {
}
