package com.school.sis.student.dto;

import com.school.sis.student.entity.AdmissionType;

public record StudentEducationalResponse(
        String elementarySchoolName,
        String elementarySchoolAddress,
        Integer elementaryYearGraduated,
        String juniorHighSchoolName,
        String juniorHighSchoolAddress,
        Integer juniorHighSchoolYearGraduated,
        String seniorHighSchoolName,
        String seniorHighSchoolAddress,
        String seniorHighSchoolStrand,
        Integer seniorHighSchoolYearGraduated,
        String previousCollege,
        String previousProgram,
        String previousSchoolYearAttended,
        AdmissionType admissionType
) {
}
