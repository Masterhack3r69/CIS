package com.school.sis.setup.dto;

import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.entity.CourseType;

import java.math.BigDecimal;
import java.util.UUID;

public record CourseResponse(
        UUID id,
        String courseCode,
        String courseTitle,
        String courseDescription,
        BigDecimal lectureHoursPerWeek,
        BigDecimal laboratoryHoursPerWeek,
        BigDecimal creditUnits,
        CourseType courseType,
        UUID departmentId,
        String departmentCode,
        ActiveStatus status
) {
}
