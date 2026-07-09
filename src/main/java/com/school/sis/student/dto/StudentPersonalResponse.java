package com.school.sis.student.dto;

import com.school.sis.student.entity.Gender;
import com.school.sis.student.entity.StudentStatus;

import java.time.LocalDate;
import java.util.UUID;

public record StudentPersonalResponse(
        UUID id,
        String studentNumber,
        String firstName,
        String middleName,
        String lastName,
        String suffix,
        String fullName,
        Gender gender,
        LocalDate birthdate,
        String birthplace,
        String civilStatus,
        String nationality,
        String religion,
        String profilePhotoPath,
        StudentStatus status
) {
}
