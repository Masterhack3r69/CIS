package com.school.sis.student.dto;

public record StudentContactResponse(
        String mobileNumber,
        String telephoneNumber,
        String emailAddress,
        String currentAddress,
        String permanentAddress,
        String province,
        String cityMunicipality,
        String barangay,
        String zipCode,
        String emergencyContactName,
        String emergencyContactNumber,
        String emergencyContactRelationship,
        String emergencyContactAddress
) {
}
