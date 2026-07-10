package com.school.sis.setup.service;

import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.response.PageResponse;
import com.school.sis.setup.dto.SchoolYearRequest;
import com.school.sis.setup.dto.SchoolYearResponse;
import com.school.sis.setup.entity.SchoolYear;
import com.school.sis.setup.repository.SchoolYearRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SchoolYearService {

    private final SchoolYearRepository schoolYearRepository;

    public SchoolYearService(SchoolYearRepository schoolYearRepository) {
        this.schoolYearRepository = schoolYearRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<SchoolYearResponse> list(Pageable pageable) {
        return PageResponse.from(schoolYearRepository.findAll(pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public SchoolYearResponse get(UUID id) {
        return toResponse(find(id));
    }

    @Transactional
    public SchoolYearResponse create(SchoolYearRequest request) {
        SchoolYear schoolYear = new SchoolYear();
        apply(schoolYear, request);
        return toResponse(schoolYearRepository.save(schoolYear));
    }

    @Transactional
    public SchoolYearResponse update(UUID id, SchoolYearRequest request) {
        SchoolYear schoolYear = find(id);
        apply(schoolYear, request);
        return toResponse(schoolYear);
    }

    private SchoolYear find(UUID id) {
        return schoolYearRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("School year not found"));
    }

    private void apply(SchoolYear schoolYear, SchoolYearRequest request) {
        schoolYear.setSchoolYear(request.schoolYear());
        schoolYear.setActive(request.active());
    }

    private SchoolYearResponse toResponse(SchoolYear schoolYear) {
        return new SchoolYearResponse(schoolYear.getId(), schoolYear.getSchoolYear(), schoolYear.isActive());
    }
}
