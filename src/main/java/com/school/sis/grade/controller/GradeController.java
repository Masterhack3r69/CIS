package com.school.sis.grade.controller;

import com.school.sis.auth.security.SisUserDetails;
import com.school.sis.common.response.ApiResponse;
import com.school.sis.common.response.PageResponse;
import com.school.sis.grade.dto.GradeClassResponse;
import com.school.sis.grade.dto.GradeEncodeRequest;
import com.school.sis.grade.dto.GradeResponse;
import com.school.sis.grade.dto.GradeSearchCriteria;
import com.school.sis.grade.entity.GradeStatus;
import com.school.sis.grade.service.GradeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/grades")
public class GradeController {

    private final GradeService gradeService;

    public GradeController(GradeService gradeService) {
        this.gradeService = gradeService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('GRADE_APPROVE')")
    public ApiResponse<PageResponse<GradeResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID studentId,
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) UUID sectionId,
            @RequestParam(required = false) UUID facultyId,
            @RequestParam(required = false) UUID schoolYearId,
            @RequestParam(required = false) UUID semesterId,
            @RequestParam(required = false) GradeStatus status,
            Pageable pageable
    ) {
        GradeSearchCriteria criteria = new GradeSearchCriteria(search, studentId, courseId, sectionId, facultyId, schoolYearId, semesterId, status);
        return ApiResponse.success("Grades retrieved", gradeService.list(criteria, pageable));
    }

    @GetMapping("/class/{scheduleId}")
    @PreAuthorize("hasAnyAuthority('GRADE_ENCODE', 'GRADE_APPROVE')")
    public ApiResponse<GradeClassResponse> classGrades(@PathVariable UUID scheduleId, @AuthenticationPrincipal SisUserDetails userDetails) {
        return ApiResponse.success("Class grades retrieved", gradeService.classGrades(scheduleId, userDetails));
    }

    @PostMapping("/class/{scheduleId}/encode")
    @PreAuthorize("hasAuthority('GRADE_ENCODE')")
    public ApiResponse<GradeClassResponse> encode(
            @PathVariable UUID scheduleId,
            @Valid @RequestBody GradeEncodeRequest request,
            @AuthenticationPrincipal SisUserDetails userDetails
    ) {
        return ApiResponse.success("Grades encoded", gradeService.encode(scheduleId, request, userDetails));
    }

    @PostMapping("/class/{scheduleId}/submit")
    @PreAuthorize("hasAuthority('GRADE_ENCODE')")
    public ApiResponse<GradeClassResponse> submit(@PathVariable UUID scheduleId, @AuthenticationPrincipal SisUserDetails userDetails) {
        return ApiResponse.success("Grades submitted", gradeService.submit(scheduleId, userDetails));
    }

    @PostMapping("/class/{scheduleId}/approve")
    @PreAuthorize("hasAuthority('GRADE_APPROVE')")
    public ApiResponse<GradeClassResponse> approve(@PathVariable UUID scheduleId, @AuthenticationPrincipal SisUserDetails userDetails) {
        return ApiResponse.success("Grades approved", gradeService.approve(scheduleId, userDetails));
    }

    @PostMapping("/class/{scheduleId}/lock")
    @PreAuthorize("hasAuthority('GRADE_APPROVE')")
    public ApiResponse<GradeClassResponse> lock(@PathVariable UUID scheduleId, @AuthenticationPrincipal SisUserDetails userDetails) {
        return ApiResponse.success("Grades locked", gradeService.lock(scheduleId, userDetails));
    }

    @GetMapping("/student/{studentId}")
    @PreAuthorize("hasAuthority('STUDENT_VIEW')")
    public ApiResponse<List<GradeResponse>> studentGrades(@PathVariable UUID studentId) {
        return ApiResponse.success("Student grades retrieved", gradeService.studentGrades(studentId));
    }
}
