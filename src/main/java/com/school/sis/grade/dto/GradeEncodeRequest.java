package com.school.sis.grade.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record GradeEncodeRequest(
        @NotEmpty @Valid List<GradeEncodeItemRequest> grades
) {
}
