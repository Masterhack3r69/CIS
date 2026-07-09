package com.school.sis.setup.controller;

import com.school.sis.common.response.ApiResponse;
import com.school.sis.common.response.PageResponse;
import com.school.sis.setup.dto.ActiveStatusRequest;
import com.school.sis.setup.dto.SectionRequest;
import com.school.sis.setup.dto.SectionResponse;
import com.school.sis.setup.service.SectionService;
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
@RequestMapping("/api/v1/sections")
public class SectionController {

    private final SectionService sectionService;

    public SectionController(SectionService sectionService) {
        this.sectionService = sectionService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_VIEW')")
    public ApiResponse<PageResponse<SectionResponse>> list(@RequestParam(required = false) String search, Pageable pageable) {
        return ApiResponse.success("Sections retrieved", sectionService.list(search, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_VIEW')")
    public ApiResponse<SectionResponse> get(@PathVariable UUID id) {
        return ApiResponse.success("Section retrieved", sectionService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_MANAGE')")
    public ApiResponse<SectionResponse> create(@Valid @RequestBody SectionRequest request) {
        return ApiResponse.success("Section created", sectionService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_MANAGE')")
    public ApiResponse<SectionResponse> update(@PathVariable UUID id, @Valid @RequestBody SectionRequest request) {
        return ApiResponse.success("Section updated", sectionService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_MANAGE')")
    public ApiResponse<SectionResponse> updateStatus(@PathVariable UUID id, @Valid @RequestBody ActiveStatusRequest request) {
        return ApiResponse.success("Section status updated", sectionService.updateStatus(id, request.status()));
    }
}
