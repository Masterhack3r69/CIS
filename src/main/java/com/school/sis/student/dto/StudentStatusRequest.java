package com.school.sis.student.dto;

import com.school.sis.student.entity.StudentStatus;
import jakarta.validation.constraints.NotNull;

public record StudentStatusRequest(@NotNull StudentStatus status) {
}
