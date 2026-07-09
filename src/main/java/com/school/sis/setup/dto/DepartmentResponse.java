package com.school.sis.setup.dto;

import com.school.sis.setup.entity.ActiveStatus;

import java.util.UUID;

public record DepartmentResponse(
        UUID id,
        String departmentCode,
        String departmentName,
        String dean,
        String description,
        ActiveStatus status
) {
}
