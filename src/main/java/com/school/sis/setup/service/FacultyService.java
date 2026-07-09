package com.school.sis.setup.service;

import com.school.sis.auth.entity.User;
import com.school.sis.auth.repository.UserRepository;
import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.response.PageResponse;
import com.school.sis.setup.dto.FacultyRequest;
import com.school.sis.setup.dto.FacultyResponse;
import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.entity.Department;
import com.school.sis.setup.entity.Faculty;
import com.school.sis.setup.repository.FacultyRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class FacultyService {

    private final FacultyRepository facultyRepository;
    private final DepartmentService departmentService;
    private final UserRepository userRepository;

    public FacultyService(FacultyRepository facultyRepository, DepartmentService departmentService, UserRepository userRepository) {
        this.facultyRepository = facultyRepository;
        this.departmentService = departmentService;
        this.userRepository = userRepository;
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
        return toResponse(facultyRepository.save(faculty));
    }

    @Transactional
    public FacultyResponse update(UUID id, FacultyRequest request) {
        Faculty faculty = find(id);
        apply(faculty, request);
        return toResponse(faculty);
    }

    @Transactional
    public FacultyResponse updateStatus(UUID id, ActiveStatus status) {
        Faculty faculty = find(id);
        faculty.setStatus(status);
        return toResponse(faculty);
    }

    Faculty find(UUID id) {
        return facultyRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Faculty not found"));
    }

    private void apply(Faculty faculty, FacultyRequest request) {
        Department department = departmentService.find(request.departmentId());
        faculty.setEmployeeNumber(request.employeeNumber());
        faculty.setFirstName(request.firstName());
        faculty.setMiddleName(request.middleName());
        faculty.setLastName(request.lastName());
        faculty.setSuffix(request.suffix());
        faculty.setEmail(request.email());
        faculty.setContactNumber(request.contactNumber());
        faculty.setUser(resolveUser(request.userId()));
        faculty.setDepartment(department);
        faculty.setEmploymentStatus(request.employmentStatus());
        faculty.setFacultyType(request.facultyType());
        faculty.setSpecialization(request.specialization());
        faculty.setStatus(request.status() == null ? ActiveStatus.ACTIVE : request.status());
    }

    private User resolveUser(UUID userId) {
        if (userId == null) {
            return null;
        }
        return userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));
    }

    private FacultyResponse toResponse(Faculty faculty) {
        User user = faculty.getUser();
        return new FacultyResponse(
                faculty.getId(),
                faculty.getEmployeeNumber(),
                faculty.getFirstName(),
                faculty.getMiddleName(),
                faculty.getLastName(),
                faculty.getSuffix(),
                faculty.getEmail(),
                faculty.getContactNumber(),
                user == null ? null : user.getId(),
                user == null ? null : user.getUsername(),
                user == null ? null : user.getEmail(),
                faculty.getDepartment().getId(),
                faculty.getDepartment().getDepartmentCode(),
                faculty.getEmploymentStatus(),
                faculty.getFacultyType(),
                faculty.getSpecialization(),
                faculty.getStatus()
        );
    }
}
