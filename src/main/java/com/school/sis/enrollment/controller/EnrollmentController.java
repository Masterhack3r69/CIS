package com.school.sis.enrollment.controller;

import com.school.sis.common.response.ApiResponse;
import com.school.sis.common.response.PageResponse;
import com.school.sis.enrollment.dto.EnrollmentRequest;
import com.school.sis.enrollment.dto.EnrollmentResponse;
import com.school.sis.enrollment.dto.EnrollmentSearchCriteria;
import com.school.sis.enrollment.dto.EnrollmentSubjectRequest;
import com.school.sis.enrollment.dto.EnrollmentSummaryResponse;
import com.school.sis.enrollment.dto.EnrollmentUpdateRequest;
import com.school.sis.enrollment.dto.EnrollmentValidationResponse;
import com.school.sis.enrollment.entity.EnrollmentStatus;
import com.school.sis.enrollment.service.EnrollmentService;
import com.school.sis.fee.dto.AssessmentResponse;
import com.school.sis.fee.service.FeeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
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
@RequestMapping("/api/v1/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final FeeService feeService;

    public EnrollmentController(EnrollmentService enrollmentService, FeeService feeService) {
        this.enrollmentService = enrollmentService;
        this.feeService = feeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ENROLLMENT_VIEW')")
    public ApiResponse<PageResponse<EnrollmentSummaryResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID programId,
            @RequestParam(required = false) UUID sectionId,
            @RequestParam(required = false) UUID schoolYearId,
            @RequestParam(required = false) UUID semesterId,
            @RequestParam(required = false) EnrollmentStatus status,
            Pageable pageable
    ) {
        EnrollmentSearchCriteria criteria = new EnrollmentSearchCriteria(search, studentId, programId, sectionId, schoolYearId, semesterId, status);
        return ApiResponse.success("Enrollments retrieved", enrollmentService.list(criteria, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ENROLLMENT_CREATE')")
    public ApiResponse<EnrollmentResponse> create(@Valid @RequestBody EnrollmentRequest request) {
        return ApiResponse.success("Enrollment created", enrollmentService.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ENROLLMENT_VIEW')")
    public ApiResponse<EnrollmentResponse> get(@PathVariable UUID id) {
        return ApiResponse.success("Enrollment retrieved", enrollmentService.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ENROLLMENT_CREATE')")
    public ApiResponse<EnrollmentResponse> update(@PathVariable UUID id, @RequestBody EnrollmentUpdateRequest request) {
        return ApiResponse.success("Enrollment updated", enrollmentService.update(id, request));
    }

    @PostMapping("/{id}/subjects")
    @PreAuthorize("hasAuthority('ENROLLMENT_CREATE')")
    public ApiResponse<EnrollmentResponse> addSubject(@PathVariable UUID id, @Valid @RequestBody EnrollmentSubjectRequest request) {
        return ApiResponse.success("Enrollment subject added", enrollmentService.addSubject(id, request));
    }

    @DeleteMapping("/{id}/subjects/{subjectId}")
    @PreAuthorize("hasAuthority('ENROLLMENT_CREATE')")
    public ApiResponse<EnrollmentResponse> dropSubject(@PathVariable UUID id, @PathVariable UUID subjectId) {
        return ApiResponse.success("Enrollment subject dropped", enrollmentService.dropSubject(id, subjectId));
    }

    @PostMapping("/{id}/validate")
    @PreAuthorize("hasAuthority('ENROLLMENT_VIEW')")
    public ApiResponse<EnrollmentValidationResponse> validate(@PathVariable UUID id) {
        return ApiResponse.success("Enrollment validation completed", enrollmentService.validate(id));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('ENROLLMENT_APPROVE')")
    public ApiResponse<EnrollmentResponse> confirm(@PathVariable UUID id) {
        return ApiResponse.success("Enrollment confirmed", enrollmentService.confirm(id));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('ENROLLMENT_APPROVE')")
    public ApiResponse<EnrollmentResponse> cancel(@PathVariable UUID id) {
        return ApiResponse.success("Enrollment cancelled", enrollmentService.cancel(id));
    }

    @PostMapping("/{id}/generate-assessment")
    @PreAuthorize("hasAuthority('FEE_MANAGE')")
    public ApiResponse<AssessmentResponse> generateAssessment(@PathVariable UUID id) {
        return ApiResponse.success("Assessment generated", feeService.generateAssessment(id));
    }
}
