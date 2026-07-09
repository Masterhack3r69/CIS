package com.school.sis.fee.dto;

import com.school.sis.setup.entity.ActiveStatus;

import java.util.List;
import java.util.UUID;

public record FeeResponse(
        UUID id,
        String feeCode,
        String feeName,
        String description,
        ActiveStatus status,
        List<FeeRuleResponse> rules
) {
}
