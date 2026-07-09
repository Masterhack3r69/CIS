package com.school.sis.setup.service;

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

    public DepartmentService(DepartmentRepository departmentRepository) {
        this.departmentRepository = departmentRepository;
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
        return toResponse(departmentRepository.save(department));
    }

    @Transactional
    public DepartmentResponse update(UUID id, DepartmentRequest request) {
        Department department = find(id);
        apply(department, request);
        return toResponse(department);
    }

    @Transactional
    public DepartmentResponse updateStatus(UUID id, ActiveStatus status) {
        Department department = find(id);
        department.setStatus(status);
        return toResponse(department);
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
