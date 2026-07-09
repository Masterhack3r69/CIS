package com.school.sis.setup.controller;

import com.school.sis.common.response.ApiResponse;
import com.school.sis.common.response.PageResponse;
import com.school.sis.setup.dto.DepartmentRequest;
import com.school.sis.setup.dto.DepartmentResponse;
import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.service.DepartmentService;
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

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/departments")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_VIEW')")
    public ApiResponse<PageResponse<DepartmentResponse>> list(@RequestParam(required = false) String search, Pageable pageable) {
        return ApiResponse.success("Departments retrieved", departmentService.list(search, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_VIEW')")
    public ApiResponse<DepartmentResponse> get(@PathVariable UUID id) {
        return ApiResponse.success("Department retrieved", departmentService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_MANAGE')")
    public ApiResponse<DepartmentResponse> create(@Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.success("Department created", departmentService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_MANAGE')")
    public ApiResponse<DepartmentResponse> update(@PathVariable UUID id, @Valid @RequestBody DepartmentRequest request) {
        return ApiResponse.success("Department updated", departmentService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_MANAGE')")
    public ApiResponse<DepartmentResponse> updateStatus(@PathVariable UUID id, @RequestBody Map<String, ActiveStatus> request) {
        return ApiResponse.success("Department status updated", departmentService.updateStatus(id, request.get("status")));
    }
}
