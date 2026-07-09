package com.school.sis.fee.dto;

import com.school.sis.setup.entity.ActiveStatus;
import jakarta.validation.constraints.NotNull;

public record FeeStatusRequest(
        @NotNull ActiveStatus status
) {
}
