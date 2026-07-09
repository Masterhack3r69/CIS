package com.school.sis.fee.dto;

import com.school.sis.fee.entity.FeeRuleType;

import java.math.BigDecimal;
import java.util.UUID;

public record AssessmentItemResponse(
        UUID id,
        UUID feeItemId,
        String feeCode,
        String feeName,
        FeeRuleType ruleType,
        BigDecimal quantity,
        BigDecimal unitAmount,
        BigDecimal lineAmount,
        Integer sortOrder
) {
}
