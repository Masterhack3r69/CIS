package com.school.sis.setup.service;

import com.school.sis.audit.AuditModule;
import com.school.sis.audit.service.AuditService;
import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.response.PageResponse;
import com.school.sis.setup.dto.FacultyRequest;
import com.school.sis.setup.dto.FacultyResponse;
import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.entity.Department;
import com.school.sis.setup.entity.Faculty;
import com.school.sis.setup.repository.DepartmentRepository;
import com.school.sis.setup.repository.FacultyRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final DepartmentRepository departmentRepository;
    private final AuditService auditService;

    public FacultyService(FacultyRepository facultyRepository, DepartmentRepository departmentRepository, AuditService auditService) {
        this.facultyRepository = facultyRepository;
        this.departmentRepository = departmentRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<FacultyResponse> list(String search, Pageable pageable) {
        String term = search == null ? "" : search;
        return PageResponse.from(facultyRepository
                .findByEmployeeNumberContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(term, term, term, pageable)
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public FacultyResponse get(UUID id) {
        return toResponse(find(id));
    }

    @Transactional
    public FacultyResponse create(FacultyRequest request) {
        Faculty faculty = new Faculty();
        apply(faculty, request);
        FacultyResponse response = toResponse(facultyRepository.save(faculty));
        auditService.log("FACULTY_CREATED", AuditModule.ACADEMIC_SETUP, "Faculty", response.id(), null, response);
        return response;
    }

    @Transactional
    public FacultyResponse update(UUID id, FacultyRequest request) {
        Faculty faculty = find(id);
        FacultyResponse before = toResponse(faculty);
        apply(faculty, request);
        FacultyResponse after = toResponse(faculty);
        auditService.log("FACULTY_UPDATED", AuditModule.ACADEMIC_SETUP, "Faculty", id, before, after);
        return after;
    }

    @Transactional
    public FacultyResponse updateStatus(UUID id, ActiveStatus status) {
        Faculty faculty = find(id);
        ActiveStatus before = faculty.getStatus();
        faculty.setStatus(status);
        FacultyResponse response = toResponse(faculty);
        auditService.log("FACULTY_STATUS_UPDATED", AuditModule.ACADEMIC_SETUP, "Faculty", id,
                java.util.Map.of("status", before), java.util.Map.of("status", status));
        return response;
    }

    private Faculty find(UUID id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Faculty not found"));
    }

    private void apply(Faculty faculty, FacultyRequest request) {
        Department department = departmentRepository.findById(request.departmentId())
                .orElseThrow(() -> new NotFoundException("Department not found"));
        faculty.setEmployeeNumber(request.employeeNumber());
        faculty.setFirstName(request.firstName());
        faculty.setMiddleName(request.middleName());
        faculty.setLastName(request.lastName());
        faculty.setSuffix(request.suffix());
        faculty.setEmail(request.email());
        faculty.setContactNumber(request.contactNumber());
        faculty.setDepartment(department);
        faculty.setEmploymentStatus(request.employmentStatus());
        faculty.setFacultyType(request.facultyType());
        faculty.setSpecialization(request.specialization());
        faculty.setStatus(request.status() == null ? ActiveStatus.ACTIVE : request.status());
    }

    private FacultyResponse toResponse(Faculty faculty) {
        return new FacultyResponse(
                faculty.getId(),
                faculty.getEmployeeNumber(),
                faculty.getFirstName(),
                faculty.getMiddleName(),
                faculty.getLastName(),
                faculty.getSuffix(),
                faculty.getEmail(),
                faculty.getContactNumber(),
                faculty.getDepartment().getId(),
                faculty.getDepartment().getDepartmentCode(),
                faculty.getEmploymentStatus(),
                faculty.getFacultyType(),
                faculty.getSpecialization(),
                faculty.getStatus()
        );
    }
}
