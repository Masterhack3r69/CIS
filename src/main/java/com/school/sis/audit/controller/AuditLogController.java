package com.school.sis.audit.controller;

import com.school.sis.audit.dto.AuditLogResponse;
import com.school.sis.audit.dto.AuditLogSearchCriteria;
import com.school.sis.audit.service.AuditService;
import com.school.sis.common.response.ApiResponse;
import com.school.sis.common.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/audit-logs")
public class AuditLogController {
    private final AuditService auditService;

    public AuditLogController(AuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('AUDIT_VIEW')")
    public ApiResponse<PageResponse<AuditLogResponse>> search(
            @RequestParam(required = false) String module,
            @RequestParam(required = false) String action,
            @RequestParam(required = false) UUID userId,
            @RequestParam(required = false) String entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant dateTo,
            Pageable pageable) {
        AuditLogSearchCriteria criteria = new AuditLogSearchCriteria(
                module, action, userId, entityType, entityId, dateFrom, dateTo
        );
        return ApiResponse.success("Audit logs retrieved", auditService.search(criteria, pageable));
    }
}
