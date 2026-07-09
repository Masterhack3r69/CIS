package com.school.sis.student.dto;

public record StudentFamilyRequest(
        String fatherName,
        String fatherOccupation,
        String fatherContactNumber,
        String motherName,
        String motherOccupation,
        String motherContactNumber,
        String guardianName,
        String guardianRelationship,
        String guardianContactNumber,
        String guardianAddress,
        String householdIncomeRange
) {
}
