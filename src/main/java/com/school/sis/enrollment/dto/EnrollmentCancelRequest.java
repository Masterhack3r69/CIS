package com.school.sis.enrollment.dto;

import jakarta.validation.constraints.NotBlank;

public record EnrollmentCancelRequest(
    @NotBlank(message = "Reason is required") String reason
) {}
