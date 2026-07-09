package com.school.sis.grade.dto;

import com.school.sis.grade.entity.GradeRemark;
import com.school.sis.grade.entity.GradeStatus;
import com.school.sis.grade.entity.SpecialGrade;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record GradeResponse(
        UUID id,
        UUID enrollmentSubjectId,
        UUID studentId,
        String studentNumber,
        String studentName,
        UUID classScheduleId,
        UUID courseId,
        String courseCode,
        String courseTitle,
        UUID sectionId,
        String sectionCode,
        UUID facultyId,
        String facultyName,
        UUID schoolYearId,
        String schoolYear,
        UUID semesterId,
        String semesterName,
        BigDecimal finalGrade,
        SpecialGrade specialGrade,
        GradeRemark remark,
        GradeStatus status,
        String notes,
        Instant encodedAt,
        Instant submittedAt,
        Instant reviewedAt,
        Instant approvedAt,
        Instant lockedAt
) {
}
