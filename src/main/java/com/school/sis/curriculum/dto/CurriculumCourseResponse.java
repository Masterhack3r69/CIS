package com.school.sis.curriculum.dto;

import com.school.sis.curriculum.entity.RequiredStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record CurriculumCourseResponse(
        UUID id,
        int yearLevel,
        String semester,
        UUID courseId,
        String courseCode,
        String courseTitle,
        BigDecimal lectureHoursPerWeek,
        BigDecimal laboratoryHoursPerWeek,
        BigDecimal creditUnits,
        int sortOrder,
        RequiredStatus requiredStatus,
        List<CourseLinkResponse> prerequisites,
        List<CourseLinkResponse> corequisites
) {
}
