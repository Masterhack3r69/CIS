package com.school.sis.grade.dto;

import com.school.sis.grade.entity.GradeStatus;

import java.util.UUID;

public record GradeSearchCriteria(
        UUID schoolYearId,
        UUID semesterId,
        UUID scheduleId,
        UUID facultyId,
        UUID studentId,
        GradeStatus status
) {
}
