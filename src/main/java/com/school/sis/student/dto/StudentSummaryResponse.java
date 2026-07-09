package com.school.sis.student.dto;

import com.school.sis.student.entity.StudentStatus;

import java.util.UUID;

public record StudentSummaryResponse(
        UUID id,
        String studentNumber,
        String fullName,
        String emailAddress,
        UUID programId,
        String programCode,
        int yearLevel,
        UUID sectionId,
        String sectionCode,
        StudentStatus status,
        String schoolYearAdmitted
) {
}
