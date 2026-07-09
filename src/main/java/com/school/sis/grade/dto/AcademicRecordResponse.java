package com.school.sis.grade.dto;

import com.school.sis.grade.entity.GradeRemark;
import com.school.sis.grade.entity.GradeStatus;
import com.school.sis.grade.entity.SpecialGrade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record AcademicRecordResponse(
        UUID id,
        UUID gradeId,
        UUID schoolYearId,
        String schoolYear,
        UUID semesterId,
        String semesterName,
        UUID courseId,
        String courseCode,
        String courseTitle,
        BigDecimal creditUnits,
        BigDecimal earnedUnits,
        BigDecimal finalGrade,
        SpecialGrade specialGrade,
        GradeRemark remark,
        GradeStatus gradeStatus,
        UUID facultyId,
        String facultyName,
        Instant approvedAt,
        Instant lockedAt
) {
}
