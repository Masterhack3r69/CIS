package com.school.sis.setup.service;
import com.school.sis.audit.AuditModule;
import com.school.sis.audit.service.AuditService;

import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.response.PageResponse;
import com.school.sis.setup.dto.ProgramRequest;
import com.school.sis.setup.dto.ProgramResponse;
import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.entity.Program;
import com.school.sis.setup.repository.ProgramRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ProgramService {

    private final ProgramRepository programRepository;
    private final DepartmentService departmentService;
    private final AuditService auditService;

    public ProgramService(ProgramRepository programRepository, DepartmentService departmentService, AuditService auditService) {
        this.programRepository = programRepository;
        this.departmentService = departmentService;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ProgramResponse> list(String search, Pageable pageable) {
        String term = search == null ? "" : search;
        return PageResponse.from(programRepository
                .findByProgramCodeContainingIgnoreCaseOrProgramNameContainingIgnoreCase(term, term, pageable)
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ProgramResponse get(UUID id) {
        return toResponse(find(id));
    }

    @Transactional
    public ProgramResponse create(ProgramRequest request) {
        Program program = new Program();
        apply(program, request);
        ProgramResponse response = toResponse(programRepository.save(program)); auditService.log("PROGRAM_CREATED", AuditModule.ACADEMIC_SETUP, "Program", response.id(), null, response); return response;
    }

    @Transactional
    public ProgramResponse update(UUID id, ProgramRequest request) {
        Program program = find(id);
        ProgramResponse before = toResponse(program);
        apply(program, request);
        ProgramResponse after = toResponse(program); auditService.log("PROGRAM_UPDATED", AuditModule.ACADEMIC_SETUP, "Program", id, before, after); return after;
    }

    Program find(UUID id) {
        return programRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Program not found"));
    }

    private void apply(Program program, ProgramRequest request) {
        program.setProgramCode(request.programCode());
        program.setProgramName(request.programName());
        program.setDepartment(departmentService.find(request.departmentId()));
        program.setDegreeType(request.degreeType());
        program.setProgramDuration(request.programDuration());
        program.setDescription(request.description());
        program.setStatus(request.status() == null ? ActiveStatus.ACTIVE : request.status());
    }

    private ProgramResponse toResponse(Program program) {
        return new ProgramResponse(
                program.getId(),
                program.getProgramCode(),
                program.getProgramName(),
                program.getDepartment().getId(),
                program.getDepartment().getDepartmentCode(),
                program.getDegreeType(),
                program.getProgramDuration(),
                program.getDescription(),
                program.getStatus()
        );
    }
}
