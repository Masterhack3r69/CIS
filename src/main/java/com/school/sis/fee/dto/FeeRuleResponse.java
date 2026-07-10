package com.school.sis.fee.dto;

import com.school.sis.fee.entity.FeeComputationType;
import com.school.sis.setup.entity.ActiveStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record FeeRuleResponse(
        UUID id,
        UUID schoolYearId,
        String schoolYear,
        UUID semesterId,
        String semesterName,
        UUID programId,
        String programCode,
        Integer yearLevel,
        FeeComputationType computationType,
        BigDecimal amount,
        ActiveStatus status
) {
}
