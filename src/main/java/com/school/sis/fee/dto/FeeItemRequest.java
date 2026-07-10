package com.school.sis.fee.dto;

import com.school.sis.fee.entity.FeeCategory;
import com.school.sis.setup.entity.ActiveStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record FeeItemRequest(
        @NotBlank String feeCode,
        @NotBlank String feeName,
        @NotNull FeeCategory category,
        String description,
        ActiveStatus status,
        @Valid List<FeeRuleRequest> rules
) {
}
