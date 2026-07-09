package com.school.sis.student.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public record StudentRequest(
        @Valid @NotNull StudentPersonalRequest personal,
        @Valid StudentContactRequest contact,
        StudentFamilyRequest family,
        StudentEducationalRequest educational,
        @Valid @NotNull StudentAcademicRequest academic
) {
}
