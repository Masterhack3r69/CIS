package com.school.sis.setup.service;
import com.school.sis.audit.AuditModule;
import com.school.sis.audit.service.AuditService;

import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.response.PageResponse;
import com.school.sis.setup.dto.SectionRequest;
import com.school.sis.setup.dto.SectionResponse;
import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.entity.Program;
import com.school.sis.setup.entity.SchoolYear;
import com.school.sis.setup.entity.Section;
import com.school.sis.setup.entity.Semester;
import com.school.sis.setup.repository.ProgramRepository;
import com.school.sis.setup.repository.SchoolYearRepository;
import com.school.sis.setup.repository.SectionRepository;
import com.school.sis.setup.repository.SemesterRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SectionService {

    private final SectionRepository sectionRepository;
    private final ProgramRepository programRepository;
    private final SchoolYearRepository schoolYearRepository;
    private final SemesterRepository semesterRepository;
    private final AuditService auditService;

    public SectionService(
            SectionRepository sectionRepository,
            ProgramRepository programRepository,
            SchoolYearRepository schoolYearRepository,
            SemesterRepository semesterRepository,
            AuditService auditService
    ) {
        this.sectionRepository = sectionRepository;
        this.programRepository = programRepository;
        this.schoolYearRepository = schoolYearRepository;
        this.semesterRepository = semesterRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<SectionResponse> list(String search, Pageable pageable) {
        String term = search == null ? "" : search;
        return PageResponse.from(sectionRepository.findBySectionCodeContainingIgnoreCase(term, pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public SectionResponse get(UUID id) {
        return toResponse(find(id));
    }

    @Transactional
    public SectionResponse create(SectionRequest request) {
        Section section = new Section();
        apply(section, request);
        SectionResponse response = toResponse(sectionRepository.save(section)); auditService.log("SECTION_CREATED", AuditModule.ACADEMIC_SETUP, "Section", response.id(), null, response); return response;
    }

    @Transactional
    public SectionResponse update(UUID id, SectionRequest request) {
        Section section = find(id);
        SectionResponse before = toResponse(section);
        apply(section, request);
        SectionResponse after = toResponse(section); auditService.log("SECTION_UPDATED", AuditModule.ACADEMIC_SETUP, "Section", id, before, after); return after;
    }

    @Transactional
    public SectionResponse updateStatus(UUID id, ActiveStatus status) {
        Section section = find(id);
        ActiveStatus before = section.getStatus();
        section.setStatus(status);
        SectionResponse response = toResponse(section); auditService.log("SECTION_STATUS_UPDATED", AuditModule.ACADEMIC_SETUP, "Section", id, java.util.Map.of("status", before), java.util.Map.of("status", status)); return response;
    }

    private Section find(UUID id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Section not found"));
    }

    private void apply(Section section, SectionRequest request) {
        Program program = programRepository.findById(request.programId())
                .orElseThrow(() -> new NotFoundException("Program not found"));
        SchoolYear schoolYear = schoolYearRepository.findById(request.schoolYearId())
                .orElseThrow(() -> new NotFoundException("School year not found"));
        Semester semester = semesterRepository.findById(request.semesterId())
                .orElseThrow(() -> new NotFoundException("Semester not found"));
        section.setSectionCode(request.sectionCode());
        section.setProgram(program);
        section.setSchoolYear(schoolYear);
        section.setSemester(semester);
        section.setYearLevel(request.yearLevel());
        section.setStatus(request.status() == null ? ActiveStatus.ACTIVE : request.status());
    }

    private SectionResponse toResponse(Section section) {
        return new SectionResponse(
                section.getId(),
                section.getSectionCode(),
                section.getProgram().getId(),
                section.getProgram().getProgramCode(),
                section.getSchoolYear().getId(),
                section.getSchoolYear().getSchoolYear(),
                section.getSemester().getId(),
                section.getSemester().getName(),
                section.getYearLevel(),
                section.getStatus()
        );
    }
}
