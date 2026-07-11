package com.school.sis.student.dto;

import jakarta.validation.constraints.Email;

public record StudentContactRequest(
        String mobileNumber, String telephoneNumber, @Email String emailAddress,
        String currentAddress, String permanentAddress,
        String currentRegionCode, String currentRegionName, String currentProvinceCode, String currentProvinceName,
        String currentCityMunicipalityCode, String currentCityMunicipalityName, String currentBarangayCode,
        String currentBarangayName, String currentZipCode,
        String permanentRegionCode, String permanentRegionName, String permanentProvinceCode, String permanentProvinceName,
        String permanentCityMunicipalityCode, String permanentCityMunicipalityName, String permanentBarangayCode,
        String permanentBarangayName, String permanentZipCode,
        String emergencyContactName, String emergencyContactNumber, String emergencyContactRelationship,
        String emergencyContactAddress
) {}
