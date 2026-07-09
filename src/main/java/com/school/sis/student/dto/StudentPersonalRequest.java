package com.school.sis.student.dto;

import com.school.sis.student.entity.Gender;
import com.school.sis.student.entity.StudentStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record StudentPersonalRequest(
        @NotBlank String studentNumber,
        @NotBlank String firstName,
        String middleName,
        @NotBlank String lastName,
        String suffix,
        Gender gender,
        @NotNull LocalDate birthdate,
        String birthplace,
        String civilStatus,
        String nationality,
        String religion,
        String profilePhotoPath,
        @NotNull StudentStatus status
) {
}
