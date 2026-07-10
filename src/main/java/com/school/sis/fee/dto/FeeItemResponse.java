package com.school.sis.fee.dto;

import com.school.sis.fee.entity.FeeCategory;
import com.school.sis.setup.entity.ActiveStatus;

import java.util.List;
import java.util.UUID;

public record FeeItemResponse(
        UUID id,
        String feeCode,
        String feeName,
        FeeCategory category,
        String description,
        ActiveStatus status,
        List<FeeRuleResponse> rules
) {
}
