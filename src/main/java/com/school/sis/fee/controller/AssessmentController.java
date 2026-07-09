package com.school.sis.fee.controller;

import com.school.sis.common.response.ApiResponse;
import com.school.sis.common.response.PageResponse;
import com.school.sis.fee.dto.AssessmentResponse;
import com.school.sis.fee.dto.AssessmentSearchCriteria;
import com.school.sis.fee.dto.AssessmentStatusRequest;
import com.school.sis.fee.dto.AssessmentSummaryResponse;
import com.school.sis.fee.entity.AssessmentStatus;
import com.school.sis.fee.service.FeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/assessments")
public class AssessmentController {

    private final FeeService feeService;

    public AssessmentController(FeeService feeService) {
        this.feeService = feeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('FEE_VIEW')")
    public ApiResponse<PageResponse<AssessmentSummaryResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID enrollmentId,
            @RequestParam(required = false) UUID schoolYearId,
            @RequestParam(required = false) UUID semesterId,
            @RequestParam(required = false) AssessmentStatus status,
            Pageable pageable
    ) {
        AssessmentSearchCriteria criteria = new AssessmentSearchCriteria(search, studentId, enrollmentId, schoolYearId, semesterId, status);
        return ApiResponse.success("Assessments retrieved", feeService.listAssessments(criteria, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('FEE_VIEW')")
    public ApiResponse<AssessmentResponse> get(@PathVariable UUID id) {
        return ApiResponse.success("Assessment retrieved", feeService.getAssessment(id));
    }

    @PostMapping("/{id}/recalculate")
    @PreAuthorize("hasAuthority('FEE_MANAGE')")
    public ApiResponse<AssessmentResponse> recalculate(@PathVariable UUID id) {
        return ApiResponse.success("Assessment recalculated", feeService.recalculateAssessment(id));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('FEE_MANAGE')")
    public ApiResponse<AssessmentResponse> updateStatus(@PathVariable UUID id, @Valid @RequestBody AssessmentStatusRequest request) {
        return ApiResponse.success("Assessment status updated", feeService.updateAssessmentStatus(id, request));
    }
}
