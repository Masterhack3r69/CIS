package com.school.sis.setup.dto;

import com.school.sis.setup.entity.ActiveStatus;
import jakarta.validation.constraints.NotNull;

public record ActiveStatusRequest(
        @NotNull ActiveStatus status
) {
}
