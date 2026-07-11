package com.school.sis.setup.service;
import com.school.sis.audit.AuditModule;
import com.school.sis.audit.service.AuditService;

import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.response.PageResponse;
import com.school.sis.setup.dto.SemesterRequest;
import com.school.sis.setup.dto.SemesterResponse;
import com.school.sis.setup.entity.Semester;
import com.school.sis.setup.repository.SemesterRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SemesterService {

    private final SemesterRepository semesterRepository;
    private final AuditService auditService;

    public SemesterService(SemesterRepository semesterRepository, AuditService auditService) {
        this.semesterRepository = semesterRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<SemesterResponse> list(Pageable pageable) {
        return PageResponse.from(semesterRepository.findAll(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public SemesterResponse get(UUID id) {
        return toResponse(find(id));
    }

    @Transactional
    public SemesterResponse create(SemesterRequest request) {
        Semester semester = new Semester();
        apply(semester, request);
        SemesterResponse response = toResponse(semesterRepository.save(semester)); auditService.log("SEMESTER_CREATED", AuditModule.ACADEMIC_SETUP, "Semester", response.id(), null, response); return response;
    }

    @Transactional
    public SemesterResponse update(UUID id, SemesterRequest request) {
        Semester semester = find(id);
        SemesterResponse before = toResponse(semester);
        apply(semester, request);
        SemesterResponse after = toResponse(semester); auditService.log("SEMESTER_UPDATED", AuditModule.ACADEMIC_SETUP, "Semester", id, before, after); return after;
    }

    private Semester find(UUID id) {
        return semesterRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Semester not found"));
    }

    private void apply(Semester semester, SemesterRequest request) {
        semester.setName(request.name());
        semester.setSortOrder(request.sortOrder());
        semester.setActive(request.active());
    }

    private SemesterResponse toResponse(Semester semester) {
        return new SemesterResponse(semester.getId(), semester.getName(), semester.getSortOrder(), semester.isActive());
    }
}
