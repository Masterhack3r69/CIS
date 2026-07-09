package com.school.sis.fee.dto;

import com.school.sis.fee.entity.FeeRuleType;
import com.school.sis.setup.entity.ActiveStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record FeeRuleRequest(
        @NotNull FeeRuleType ruleType,
        @NotNull @DecimalMin("0.0") BigDecimal amount,
        UUID programId,
        UUID schoolYearId,
        UUID semesterId,
        @Min(1) Integer yearLevel,
        ActiveStatus status
) {
}
