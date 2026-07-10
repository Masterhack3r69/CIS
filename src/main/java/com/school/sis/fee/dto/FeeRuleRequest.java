package com.school.sis.fee.dto;

import com.school.sis.fee.entity.FeeComputationType;
import com.school.sis.setup.entity.ActiveStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record FeeRuleRequest(
        UUID id,
        @NotNull UUID schoolYearId,
        UUID semesterId,
        UUID programId,
        @Min(1) Integer yearLevel,
        @NotNull FeeComputationType computationType,
        @NotNull @DecimalMin("0.00") BigDecimal amount,
        ActiveStatus status
) {
}
