package com.school.sis.grade.dto;

import java.util.List;
import java.util.UUID;

public record ClassGradeSheetResponse(
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
        int studentCount,
        List<ClassGradeRowResponse> rows
) {
}
