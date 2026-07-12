package com.school.sis.setup.service;
import com.school.sis.audit.AuditModule;
import com.school.sis.audit.service.AuditService;

import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.exception.BusinessRuleException;
import com.school.sis.common.response.PageResponse;
import com.school.sis.curriculum.entity.Curriculum;
import com.school.sis.curriculum.repository.CurriculumRepository;
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
    private final CurriculumRepository curriculumRepository;
    private final SchoolYearRepository schoolYearRepository;
    private final SemesterRepository semesterRepository;
    private final AuditService auditService;

    public SectionService(
            SectionRepository sectionRepository,
            ProgramRepository programRepository,
            CurriculumRepository curriculumRepository,
            SchoolYearRepository schoolYearRepository,
            SemesterRepository semesterRepository,
            AuditService auditService
    ) {
        this.sectionRepository = sectionRepository;
        this.programRepository = programRepository;
        this.curriculumRepository = curriculumRepository;
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
        if (sectionRepository.existsBySectionCodeAndSchoolYearIdAndSemesterId(request.sectionCode(), request.schoolYearId(), request.semesterId())) {
            throw new BusinessRuleException("Section code already exists in this term");
        }
        Section section = new Section();
        apply(section, request);
        SectionResponse response = toResponse(sectionRepository.save(section)); auditService.log("SECTION_CREATED", AuditModule.ACADEMIC_SETUP, "Section", response.id(), null, response); return response;
    }

    @Transactional
    public SectionResponse update(UUID id, SectionRequest request) {
        if (sectionRepository.existsBySectionCodeAndSchoolYearIdAndSemesterIdAndIdNot(request.sectionCode(), request.schoolYearId(), request.semesterId(), id)) {
            throw new BusinessRuleException("Section code already exists in this term");
        }
        Section section = find(id);
        SectionResponse before = toResponse(section);
        apply(section, request);
        SectionResponse after = toResponse(section); auditService.log("SECTION_UPDATED", AuditModule.ACADEMIC_SETUP, "Section", id, before, after); return after;
    }

    @Transactional
    public SectionResponse updateStatus(UUID id, ActiveStatus status) {
        Section section = find(id);
        if (status == ActiveStatus.ACTIVE && section.getCurriculum() == null) {
            throw new BusinessRuleException("Assign a curriculum before activating this section");
        }
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
        Curriculum curriculum = curriculumRepository.findById(request.curriculumId())
                .orElseThrow(() -> new NotFoundException("Curriculum not found"));
        if (!curriculum.getProgram().getId().equals(program.getId())) {
            throw new BusinessRuleException("Curriculum does not belong to the selected program");
        }
        SchoolYear schoolYear = schoolYearRepository.findById(request.schoolYearId())
                .orElseThrow(() -> new NotFoundException("School year not found"));
        Semester semester = semesterRepository.findById(request.semesterId())
                .orElseThrow(() -> new NotFoundException("Semester not found"));
        section.setSectionCode(request.sectionCode());
        section.setProgram(program);
        section.setCurriculum(curriculum);
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
                section.getCurriculum() == null ? null : section.getCurriculum().getId(),
                section.getCurriculum() == null ? null : section.getCurriculum().getCurriculumCode(),
                section.getSchoolYear().getId(),
                section.getSchoolYear().getSchoolYear(),
                section.getSemester().getId(),
                section.getSemester().getName(),
                section.getYearLevel(),
                section.getStatus()
        );
    }
}
