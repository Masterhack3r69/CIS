package com.school.sis.setup.controller;

import com.school.sis.common.response.ApiResponse;
import com.school.sis.common.response.PageResponse;
import com.school.sis.setup.dto.ProgramRequest;
import com.school.sis.setup.dto.ProgramResponse;
import com.school.sis.setup.service.ProgramService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/programs")
public class ProgramController {

    private final ProgramService programService;

    public ProgramController(ProgramService programService) {
        this.programService = programService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_VIEW')")
    public ApiResponse<PageResponse<ProgramResponse>> list(@RequestParam(required = false) String search, Pageable pageable) {
        return ApiResponse.success("Programs retrieved", programService.list(search, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_VIEW')")
    public ApiResponse<ProgramResponse> get(@PathVariable UUID id) {
        return ApiResponse.success("Program retrieved", programService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_MANAGE')")
    public ApiResponse<ProgramResponse> create(@Valid @RequestBody ProgramRequest request) {
        return ApiResponse.success("Program created", programService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_MANAGE')")
    public ApiResponse<ProgramResponse> update(@PathVariable UUID id, @Valid @RequestBody ProgramRequest request) {
        return ApiResponse.success("Program updated", programService.update(id, request));
    }
}
