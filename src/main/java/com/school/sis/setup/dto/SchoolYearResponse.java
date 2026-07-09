package com.school.sis.setup.dto;

import java.util.UUID;

public record SchoolYearResponse(
        UUID id,
        String schoolYear,
        boolean active
) {
}
