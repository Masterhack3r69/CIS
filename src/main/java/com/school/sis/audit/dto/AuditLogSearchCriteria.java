package com.school.sis.audit.dto;

import java.time.Instant;
import java.util.UUID;

public record AuditLogSearchCriteria(
        String module,
        String action,
        UUID userId,
        String entityType,
        UUID entityId,
        Instant dateFrom,
        Instant dateTo
) {
}
