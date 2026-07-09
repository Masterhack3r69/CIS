package com.school.sis.curriculum.dto;

import java.util.List;

public record CurriculumChecklistResponse(
        CurriculumResponse curriculum,
        List<CurriculumTermResponse> terms
) {
}
