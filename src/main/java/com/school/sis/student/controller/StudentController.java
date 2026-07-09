package com.school.sis.student.controller;

import com.school.sis.auth.security.SisUserDetails;
import com.school.sis.common.response.ApiResponse;
import com.school.sis.common.response.PageResponse;
import com.school.sis.student.dto.DocumentVerificationRequest;
import com.school.sis.student.dto.StudentAcademicRecordsResponse;
import com.school.sis.student.dto.StudentDocumentResponse;
import com.school.sis.student.dto.StudentRequest;
import com.school.sis.student.dto.StudentResponse;
import com.school.sis.student.dto.StudentSearchCriteria;
import com.school.sis.student.dto.StudentStatusRequest;
import com.school.sis.student.dto.StudentSummaryResponse;
import com.school.sis.student.entity.DocumentVerificationStatus;
import com.school.sis.student.entity.StudentStatus;
import com.school.sis.student.service.StudentService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/students")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('STUDENT_VIEW')")
    public ApiResponse<PageResponse<StudentSummaryResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID programId,
            @RequestParam(required = false) Integer yearLevel,
            @RequestParam(required = false) UUID sectionId,
            @RequestParam(required = false) StudentStatus status,
            @RequestParam(required = false) String schoolYearAdmitted,
            @RequestParam(required = false) DocumentVerificationStatus documentStatus,
            Pageable pageable
    ) {
        StudentSearchCriteria criteria = new StudentSearchCriteria(search, programId, yearLevel, sectionId, status, schoolYearAdmitted, documentStatus);
        return ApiResponse.success("Students retrieved", studentService.list(criteria, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('STUDENT_CREATE')")
    public ApiResponse<StudentResponse> create(@Valid @RequestBody StudentRequest request) {
        return ApiResponse.success("Student created", studentService.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STUDENT_VIEW')")
    public ApiResponse<StudentResponse> get(@PathVariable UUID id) {
        return ApiResponse.success("Student retrieved", studentService.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('STUDENT_UPDATE')")
    public ApiResponse<StudentResponse> update(@PathVariable UUID id, @Valid @RequestBody StudentRequest request) {
        return ApiResponse.success("Student updated", studentService.update(id, request));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority('STUDENT_UPDATE')")
    public ApiResponse<StudentResponse> updateStatus(@PathVariable UUID id, @Valid @RequestBody StudentStatusRequest request) {
        return ApiResponse.success("Student status updated", studentService.updateStatus(id, request));
    }

    @PostMapping("/{id}/documents")
    @PreAuthorize("hasAuthority('STUDENT_UPDATE')")
    public ApiResponse<StudentDocumentResponse> uploadDocument(
            @PathVariable UUID id,
            @RequestParam String documentType,
            @RequestParam(required = false) String remarks,
            @RequestPart MultipartFile file,
            @AuthenticationPrincipal SisUserDetails userDetails
    ) {
        return ApiResponse.success("Student document uploaded", studentService.uploadDocument(id, documentType, remarks, file, userDetails));
    }

    @GetMapping("/{id}/documents")
    @PreAuthorize("hasAuthority('STUDENT_VIEW')")
    public ApiResponse<List<StudentDocumentResponse>> listDocuments(@PathVariable UUID id) {
        return ApiResponse.success("Student documents retrieved", studentService.listDocuments(id));
    }

    @PatchMapping("/{id}/documents/{documentId}/verify")
    @PreAuthorize("hasAuthority('STUDENT_UPDATE')")
    public ApiResponse<StudentDocumentResponse> verifyDocument(
            @PathVariable UUID id,
            @PathVariable UUID documentId,
            @Valid @RequestBody DocumentVerificationRequest request,
            @AuthenticationPrincipal SisUserDetails userDetails
    ) {
        return ApiResponse.success("Student document verification updated", studentService.verifyDocument(id, documentId, request, userDetails));
    }

    @GetMapping("/{id}/academic-records")
    @PreAuthorize("hasAuthority('STUDENT_VIEW')")
    public ApiResponse<StudentAcademicRecordsResponse> academicRecords(@PathVariable UUID id) {
        return ApiResponse.success("Student academic records retrieved", studentService.academicRecords(id));
    }
}
