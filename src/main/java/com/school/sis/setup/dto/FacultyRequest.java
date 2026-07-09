package com.school.sis.setup.dto;

import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.entity.EmploymentStatus;
import com.school.sis.setup.entity.FacultyType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record FacultyRequest(
        @NotBlank String employeeNumber,
        @NotBlank String firstName,
        String middleName,
        @NotBlank String lastName,
        String suffix,
        @NotBlank @Email String email,
        String contactNumber,
        @NotNull UUID departmentId,
        @NotNull EmploymentStatus employmentStatus,
        @NotNull FacultyType facultyType,
        String specialization,
        ActiveStatus status
) {
}
