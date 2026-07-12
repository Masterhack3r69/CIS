package com.school.sis.student.dto;

import com.school.sis.student.entity.AcademicStatus;
import com.school.sis.student.entity.StudentClassification;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;

public record StudentAcademicRequest(
        @NotNull UUID programId,
        @NotNull UUID curriculumId,
        @Min(1) int yearLevel,
        @NotNull LocalDate dateAdmitted,
        @NotBlank String schoolYearAdmitted,
        StudentClassification classification,
        AcademicStatus academicStatus
) {
}
