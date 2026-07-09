package com.school.sis.student.dto;

public record StudentResponse(
        StudentPersonalResponse personal,
        StudentContactResponse contact,
        StudentFamilyResponse family,
        StudentEducationalResponse educational,
        StudentAcademicResponse academic
) {
}
