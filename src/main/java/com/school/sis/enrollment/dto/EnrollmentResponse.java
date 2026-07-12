package com.school.sis.enrollment.dto;

import com.school.sis.enrollment.entity.EnrollmentStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record EnrollmentResponse(
        UUID id,
        UUID studentId,
        String studentNumber,
        String studentName,
        UUID programId,
        String programCode,
        int yearLevel,
        UUID sectionId,
        String sectionCode,
        UUID schoolYearId,
        String schoolYear,
        UUID semesterId,
        String semesterName,
        EnrollmentStatus status,
        String remarks,
        BigDecimal totalCreditUnits,
        int subjectCount,
        List<EnrollmentSubjectResponse> subjects,
        EnrollmentValidationResponse validation
) {
}
