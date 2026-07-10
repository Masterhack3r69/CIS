package com.school.sis.grade.dto;

import java.util.List;
import java.util.UUID;

public record GradeClassResponse(
        UUID scheduleId,
        UUID courseId,
        String courseCode,
        String courseTitle,
        UUID sectionId,
        String sectionCode,
        UUID facultyId,
        String facultyName,
        List<GradeResponse> grades
) {
}
