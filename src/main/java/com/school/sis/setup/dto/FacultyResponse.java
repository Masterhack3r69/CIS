package com.school.sis.setup.dto;

import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.entity.EmploymentStatus;
import com.school.sis.setup.entity.FacultyType;

import java.util.UUID;

public record FacultyResponse(
        UUID id,
        String employeeNumber,
        String firstName,
        String middleName,
        String lastName,
        String suffix,
        String email,
        String contactNumber,
        UUID userId,
        String username,
        String userEmail,
        UUID departmentId,
        String departmentCode,
        EmploymentStatus employmentStatus,
        FacultyType facultyType,
        String specialization,
        ActiveStatus status
) {
}
