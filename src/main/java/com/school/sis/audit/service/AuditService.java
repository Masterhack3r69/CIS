package com.school.sis.audit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.school.sis.audit.dto.AuditLogResponse;
import com.school.sis.audit.dto.AuditLogSearchCriteria;
import com.school.sis.audit.entity.AuditLog;
import com.school.sis.audit.repository.AuditLogRepository;
import com.school.sis.auth.entity.User;
import com.school.sis.auth.repository.UserRepository;
import com.school.sis.auth.security.SisUserDetails;
import com.school.sis.common.response.PageResponse;
import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.UUID;

@Service
public class AuditService {
    private static final Sort DEFAULT_SORT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    public AuditService(AuditLogRepository auditLogRepository, UserRepository userRepository, ObjectMapper objectMapper) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public AuditLog log(String action, String module, String entityType, UUID entityId, Object oldValue, Object newValue) {
        return log(resolveCurrentUser(), action, module, entityType, entityId, oldValue, newValue);
    }

    @Transactional
    public AuditLog log(SisUserDetails userDetails, String action, String module, String entityType, UUID entityId,
                        Object oldValue, Object newValue) {
        User user = userDetails == null ? null : userRepository.findById(userDetails.id()).orElse(null);
        return log(user, action, module, entityType, entityId, oldValue, newValue);
    }

    @Transactional
    public AuditLog log(User user, String action, String module, String entityType, UUID entityId,
                        Object oldValue, Object newValue) {
        AuditLog log = new AuditLog();
        log.setUser(user);
        log.setAction(action);
        log.setModule(module);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        log.setOldValue(toJson(oldValue));
        log.setNewValue(toJson(newValue));
        populateRequestContext(log);
        return auditLogRepository.save(log);
    }

    @Transactional(readOnly = true)
    public PageResponse<AuditLogResponse> search(AuditLogSearchCriteria criteria, Pageable pageable) {
        Pageable effectivePageable = pageable.getSort().isSorted()
                ? pageable
                : PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), DEFAULT_SORT);
        return PageResponse.from(auditLogRepository.findAll(specification(criteria), effectivePageable)
                .map(AuditLogResponse::from));
    }

    private Specification<AuditLog> specification(AuditLogSearchCriteria criteria) {
        return (root, query, cb) -> {
            var predicates = new ArrayList<Predicate>();
            if (hasText(criteria.module())) predicates.add(cb.equal(root.get("module"), criteria.module()));
            if (hasText(criteria.action())) predicates.add(cb.equal(root.get("action"), criteria.action()));
            if (criteria.userId() != null) predicates.add(cb.equal(root.get("user").get("id"), criteria.userId()));
            if (hasText(criteria.entityType())) predicates.add(cb.equal(root.get("entityType"), criteria.entityType()));
            if (criteria.entityId() != null) predicates.add(cb.equal(root.get("entityId"), criteria.entityId()));
            if (criteria.dateFrom() != null) predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), criteria.dateFrom()));
            if (criteria.dateTo() != null) predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), criteria.dateTo()));
            return cb.and(predicates.toArray(Predicate[]::new));
        };
    }

    private User resolveCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof SisUserDetails userDetails)) {
            return null;
        }
        return userRepository.findById(userDetails.id()).orElse(null);
    }

    private JsonNode toJson(Object value) {
        if (value == null) return null;
        if (value instanceof JsonNode jsonNode) return jsonNode;
        return objectMapper.valueToTree(value);
    }

    private void populateRequestContext(AuditLog log) {
        if (!(RequestContextHolder.getRequestAttributes() instanceof ServletRequestAttributes attributes)) {
            return;
        }
        HttpServletRequest request = attributes.getRequest();
        log.setIpAddress(clientIp(request));
        log.setUserAgent(request.getHeader("User-Agent"));
    }

    private String clientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (hasText(forwardedFor)) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
