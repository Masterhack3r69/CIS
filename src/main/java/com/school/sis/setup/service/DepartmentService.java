package com.school.sis.setup.service;
import com.school.sis.audit.AuditModule;
import com.school.sis.audit.service.AuditService;

import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.response.PageResponse;
import com.school.sis.setup.dto.DepartmentRequest;
import com.school.sis.setup.dto.DepartmentResponse;
import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.entity.Department;
import com.school.sis.setup.repository.DepartmentRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final AuditService auditService;

    public DepartmentService(DepartmentRepository departmentRepository, AuditService auditService) {
        this.departmentRepository = departmentRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<DepartmentResponse> list(String search, Pageable pageable) {
        String term = search == null ? "" : search;
        return PageResponse.from(departmentRepository
                .findByDepartmentCodeContainingIgnoreCaseOrDepartmentNameContainingIgnoreCase(term, term, pageable)
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public DepartmentResponse get(UUID id) {
        return toResponse(find(id));
    }

    @Transactional
    public DepartmentResponse create(DepartmentRequest request) {
        Department department = new Department();
        apply(department, request);
        DepartmentResponse response = toResponse(departmentRepository.save(department)); auditService.log("DEPARTMENT_CREATED", AuditModule.ACADEMIC_SETUP, "Department", response.id(), null, response); return response;
    }

    @Transactional
    public DepartmentResponse update(UUID id, DepartmentRequest request) {
        Department department = find(id);
        DepartmentResponse before = toResponse(department);
        apply(department, request);
        DepartmentResponse after = toResponse(department); auditService.log("DEPARTMENT_UPDATED", AuditModule.ACADEMIC_SETUP, "Department", id, before, after); return after;
    }

    @Transactional
    public DepartmentResponse updateStatus(UUID id, ActiveStatus status) {
        Department department = find(id);
        ActiveStatus before = department.getStatus();
        department.setStatus(status);
        DepartmentResponse response = toResponse(department); auditService.log("DEPARTMENT_STATUS_UPDATED", AuditModule.ACADEMIC_SETUP, "Department", id, java.util.Map.of("status", before), java.util.Map.of("status", status)); return response;
    }

    Department find(UUID id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Department not found"));
    }

    private void apply(Department department, DepartmentRequest request) {
        department.setDepartmentCode(request.departmentCode());
        department.setDepartmentName(request.departmentName());
        department.setDean(request.dean());
        department.setDescription(request.description());
        department.setStatus(request.status() == null ? ActiveStatus.ACTIVE : request.status());
    }

    private DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.getId(),
                department.getDepartmentCode(),
                department.getDepartmentName(),
                department.getDean(),
                department.getDescription(),
                department.getStatus()
        );
    }
}
