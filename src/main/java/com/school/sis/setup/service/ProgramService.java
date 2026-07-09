package com.school.sis.setup.service;

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

    public ProgramService(ProgramRepository programRepository, DepartmentService departmentService) {
        this.programRepository = programRepository;
        this.departmentService = departmentService;
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
        return toResponse(programRepository.save(program));
    }

    @Transactional
    public ProgramResponse update(UUID id, ProgramRequest request) {
        Program program = find(id);
        apply(program, request);
        return toResponse(program);
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
