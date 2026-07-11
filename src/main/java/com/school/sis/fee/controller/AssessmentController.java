package com.school.sis.fee.controller;

import com.school.sis.common.response.ApiResponse;
import com.school.sis.common.response.PageResponse;
import com.school.sis.fee.dto.AssessmentResponse;
import com.school.sis.fee.dto.AssessmentSearchCriteria;
import com.school.sis.fee.dto.AssessmentStatusRequest;
import com.school.sis.fee.dto.AssessmentSummaryResponse;
import com.school.sis.fee.entity.AssessmentStatus;
import com.school.sis.fee.service.AssessmentService;
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
@RequestMapping("/api/v1")
public class AssessmentController {

    private final AssessmentService assessmentService;

    public AssessmentController(AssessmentService assessmentService) {
        this.assessmentService = assessmentService;
    }

    @GetMapping("/assessments")
    @PreAuthorize("hasAuthority('FEE_MANAGE')")
    public ApiResponse<PageResponse<AssessmentSummaryResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID schoolYearId,
            @RequestParam(required = false) UUID semesterId,
            @RequestParam(required = false) AssessmentStatus status,
            Pageable pageable
    ) {
        AssessmentSearchCriteria criteria = new AssessmentSearchCriteria(search, studentId, schoolYearId, semesterId, status);
        return ApiResponse.success("Assessments retrieved", assessmentService.list(criteria, pageable));
    }

    @GetMapping("/assessments/{id}")
    @PreAuthorize("hasAuthority('FEE_MANAGE')")
    public ApiResponse<AssessmentResponse> get(@PathVariable UUID id) {
        return ApiResponse.success("Assessment retrieved", assessmentService.get(id));
    }

    @GetMapping("/enrollments/{enrollmentId}/assessment")
    @PreAuthorize("hasAnyAuthority('FEE_MANAGE', 'ENROLLMENT_VIEW')")
    public ApiResponse<AssessmentResponse> getByEnrollment(@PathVariable UUID enrollmentId) {
        return ApiResponse.success("Assessment retrieved", assessmentService.getByEnrollment(enrollmentId));
    }

    @PostMapping("/enrollments/{enrollmentId}/generate-assessment")
    @PreAuthorize("hasAuthority('FEE_MANAGE')")
    public ApiResponse<AssessmentResponse> generate(@PathVariable UUID enrollmentId) {
        return ApiResponse.success("Assessment generated", assessmentService.generate(enrollmentId));
    }

    @PostMapping("/assessments/{id}/recalculate")
    @PreAuthorize("hasAuthority('FEE_MANAGE')")
    public ApiResponse<AssessmentResponse> recalculate(@PathVariable UUID id) {
        return ApiResponse.success("Assessment recalculated", assessmentService.recalculate(id));
    }

    @PatchMapping("/assessments/{id}/status")
    @PreAuthorize("hasAuthority('FEE_MANAGE')")
    public ApiResponse<AssessmentResponse> updateStatus(@PathVariable UUID id, @Valid @RequestBody AssessmentStatusRequest request) {
        return ApiResponse.success("Assessment status updated", assessmentService.updateStatus(id, request));
    }
}
