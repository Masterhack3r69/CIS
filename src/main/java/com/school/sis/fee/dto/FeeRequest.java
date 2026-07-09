package com.school.sis.fee.dto;

import com.school.sis.setup.entity.ActiveStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import java.util.List;

public record FeeRequest(
        @NotBlank String feeCode,
        @NotBlank String feeName,
        String description,
        ActiveStatus status,
        @Valid List<FeeRuleRequest> rules
) {
}
