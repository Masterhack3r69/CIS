package com.school.sis.auth.dto;

import jakarta.validation.constraints.NotNull;

public record UserStatusRequest(
        @NotNull Boolean active
) {
}
