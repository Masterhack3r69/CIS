package com.school.sis.student.dto;

import jakarta.validation.constraints.Email;

public record StudentContactRequest(
        String mobileNumber,
        String telephoneNumber,
        @Email String emailAddress,
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
