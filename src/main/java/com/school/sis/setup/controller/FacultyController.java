package com.school.sis.setup.controller;

import com.school.sis.common.response.ApiResponse;
import com.school.sis.common.response.PageResponse;
import com.school.sis.setup.dto.ActiveStatusRequest;
import com.school.sis.setup.dto.FacultyRequest;
import com.school.sis.setup.dto.FacultyResponse;
import com.school.sis.setup.service.FacultyService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/faculty")
public class FacultyController {

    private final FacultyService facultyService;

    public FacultyController(FacultyService facultyService) {
        this.facultyService = facultyService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_VIEW')")
    public ApiResponse<PageResponse<FacultyResponse>> list(@RequestParam(required = false) String search, Pageable pageable) {
        return ApiResponse.success("Faculty retrieved", facultyService.list(search, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_VIEW')")
    public ApiResponse<FacultyResponse> get(@PathVariable UUID id) {
        return ApiResponse.success("Faculty retrieved", facultyService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_MANAGE')")
    public ApiResponse<FacultyResponse> create(@Valid @RequestBody FacultyRequest request) {
        return ApiResponse.success("Faculty created", facultyService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_MANAGE')")
    public ApiResponse<FacultyResponse> update(@PathVariable UUID id, @Valid @RequestBody FacultyRequest request) {
        return ApiResponse.success("Faculty updated", facultyService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_MANAGE')")
    public ApiResponse<FacultyResponse> updateStatus(@PathVariable UUID id, @Valid @RequestBody ActiveStatusRequest request) {
        return ApiResponse.success("Faculty status updated", facultyService.updateStatus(id, request.status()));
    }
}
