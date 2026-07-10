package com.school.sis.fee.dto;

import com.school.sis.fee.entity.FeeCategory;
import com.school.sis.setup.entity.ActiveStatus;

public record FeeSearchCriteria(
        String search,
        FeeCategory category,
        ActiveStatus status
) {
}
