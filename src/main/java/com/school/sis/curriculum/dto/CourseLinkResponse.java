package com.school.sis.curriculum.dto;

import java.util.UUID;

public record CourseLinkResponse(
        UUID id,
        String courseCode,
        String courseTitle
) {
}
