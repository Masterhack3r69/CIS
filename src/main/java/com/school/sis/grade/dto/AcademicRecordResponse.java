package com.school.sis.grade.dto;

import com.school.sis.grade.entity.GradeRemark;
import com.school.sis.grade.entity.GradeStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AcademicRecordResponse(
        UUID id,
        UUID gradeId,
        UUID courseId,
        String courseCode,
        String courseTitle,
        BigDecimal creditUnits,
        BigDecimal earnedUnits,
        BigDecimal finalGrade,
        GradeRemark remarks,
        GradeStatus gradeStatus,
        UUID sectionId,
        String sectionCode,
        UUID facultyId,
        String facultyName,
        UUID schoolYearId,
        String schoolYear,
        UUID semesterId,
        String semesterName,
        Instant approvedAt,
        Instant lockedAt
) {
}
