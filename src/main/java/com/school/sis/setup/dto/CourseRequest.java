package com.school.sis.setup.dto;

import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.entity.CourseType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CourseRequest(
        @NotBlank String courseCode,
        @NotBlank String courseTitle,
        String courseDescription,
        @NotNull @DecimalMin("0.0") BigDecimal lectureHoursPerWeek,
        @NotNull @DecimalMin("0.0") BigDecimal laboratoryHoursPerWeek,
        @NotNull @DecimalMin("0.0") BigDecimal creditUnits,
        @NotNull CourseType courseType,
        @NotNull UUID departmentId,
        ActiveStatus status
) {
}
