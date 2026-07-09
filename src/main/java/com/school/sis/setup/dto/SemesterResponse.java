package com.school.sis.setup.dto;

import java.util.UUID;

public record SemesterResponse(
        UUID id,
        String name,
        int sortOrder,
        boolean active
) {
}
