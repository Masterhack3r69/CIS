package com.school.sis.curriculum.dto;

import java.math.BigDecimal;
import java.util.List;

public record CurriculumTermResponse(
        int yearLevel,
        String semester,
        BigDecimal totalLectureHours,
        BigDecimal totalLaboratoryHours,
        BigDecimal totalCreditUnits,
        List<CurriculumCourseResponse> courses
) {
}
