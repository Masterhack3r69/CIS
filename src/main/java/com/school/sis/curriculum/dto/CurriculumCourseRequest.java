package com.school.sis.curriculum.dto;

import com.school.sis.curriculum.entity.RequiredStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CurriculumCourseRequest(
        @Min(1) int yearLevel,
        @NotBlank String semester,
        @NotNull UUID courseId,
        @Min(1) int sortOrder,
        @NotNull RequiredStatus requiredStatus,
        List<UUID> prerequisiteCourseIds,
        List<UUID> corequisiteCourseIds
) {
}
