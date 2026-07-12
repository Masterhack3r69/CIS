package com.school.sis.student.dto;

import com.school.sis.student.entity.DocumentVerificationStatus;
import com.school.sis.student.entity.StudentStatus;

import java.util.UUID;

public record StudentSearchCriteria(
        String search,
        UUID programId,
        Integer yearLevel,
        StudentStatus status,
        String schoolYearAdmitted,
        DocumentVerificationStatus documentStatus
) {
}
