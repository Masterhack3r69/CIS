package com.school.sis.fee.dto;

import com.school.sis.fee.entity.FeeCategory;
import com.school.sis.setup.entity.ActiveStatus;

import java.util.UUID;

public record FeeItemSummaryResponse(
        UUID id,
        String feeCode,
        String feeName,
        FeeCategory category,
        ActiveStatus status
) {
}
