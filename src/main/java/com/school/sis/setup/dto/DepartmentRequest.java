package com.school.sis.setup.dto;

import com.school.sis.setup.entity.ActiveStatus;
import jakarta.validation.constraints.NotBlank;

public record DepartmentRequest(
        @NotBlank String departmentCode,
        @NotBlank String departmentName,
        String dean,
        String description,
        ActiveStatus status
) {
}
