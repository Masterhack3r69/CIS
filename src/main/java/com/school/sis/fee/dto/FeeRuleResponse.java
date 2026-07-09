package com.school.sis.fee.dto;

import com.school.sis.fee.entity.FeeRuleType;
import com.school.sis.setup.entity.ActiveStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record FeeRuleResponse(
        UUID id,
        FeeRuleType ruleType,
        BigDecimal amount,
        UUID programId,
        String programCode,
        UUID schoolYearId,
        String schoolYear,
        UUID semesterId,
        String semesterName,
        Integer yearLevel,
        ActiveStatus status
) {
}
