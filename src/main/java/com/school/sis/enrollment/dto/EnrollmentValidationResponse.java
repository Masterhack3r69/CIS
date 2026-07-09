package com.school.sis.enrollment.dto;

import java.math.BigDecimal;
import java.util.List;

public record EnrollmentValidationResponse(
        boolean valid,
        List<EnrollmentValidationIssueResponse> blockingIssues,
        List<EnrollmentValidationIssueResponse> warnings,
        BigDecimal totalCreditUnits,
        int subjectCount
) {
}
