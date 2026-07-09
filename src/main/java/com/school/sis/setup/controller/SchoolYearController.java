package com.school.sis.setup.controller;

import com.school.sis.common.response.ApiResponse;
import com.school.sis.common.response.PageResponse;
import com.school.sis.setup.dto.SchoolYearRequest;
import com.school.sis.setup.dto.SchoolYearResponse;
import com.school.sis.setup.service.SchoolYearService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/school-years")
public class SchoolYearController {

    private final SchoolYearService schoolYearService;

    public SchoolYearController(SchoolYearService schoolYearService) {
        this.schoolYearService = schoolYearService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_VIEW')")
    public ApiResponse<PageResponse<SchoolYearResponse>> list(Pageable pageable) {
        return ApiResponse.success("School years retrieved", schoolYearService.list(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_VIEW')")
    public ApiResponse<SchoolYearResponse> get(@PathVariable UUID id) {
        return ApiResponse.success("School year retrieved", schoolYearService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_MANAGE')")
    public ApiResponse<SchoolYearResponse> create(@Valid @RequestBody SchoolYearRequest request) {
        return ApiResponse.success("School year created", schoolYearService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_MANAGE')")
    public ApiResponse<SchoolYearResponse> update(@PathVariable UUID id, @Valid @RequestBody SchoolYearRequest request) {
        return ApiResponse.success("School year updated", schoolYearService.update(id, request));
    }
}
