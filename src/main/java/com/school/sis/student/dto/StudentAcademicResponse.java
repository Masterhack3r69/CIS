package com.school.sis.student.dto;

import com.school.sis.student.entity.AcademicStatus;
import com.school.sis.student.entity.StudentClassification;

import java.time.LocalDate;
import java.util.UUID;

public record StudentAcademicResponse(
        UUID programId,
        String programCode,
        UUID curriculumId,
        String curriculumCode,
        int yearLevel,
        String semester,
        UUID sectionId,
        String sectionCode,
        LocalDate dateAdmitted,
        String schoolYearAdmitted,
        StudentClassification classification,
        AcademicStatus academicStatus
) {
}
