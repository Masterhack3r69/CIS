package com.school.sis.student.dto;

import com.school.sis.grade.dto.AcademicRecordResponse;

import java.util.List;
import java.util.UUID;

public record StudentAcademicRecordsResponse(
        UUID studentId,
        String studentNumber,
        String fullName,
        UUID programId,
        String programCode,
        UUID curriculumId,
        String curriculumCode,
        List<AcademicRecordResponse> records
) {
}
