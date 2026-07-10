package com.school.sis.report.controller;

import com.school.sis.auth.security.SisUserDetails;
import com.school.sis.report.service.PdfReport;
import com.school.sis.report.service.ReportService;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reports")
public class ReportController {

    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping("/students/{id}/profile")
    @PreAuthorize("hasAuthority('REPORT_GENERATE')")
    public ResponseEntity<byte[]> studentProfile(@PathVariable UUID id, @AuthenticationPrincipal SisUserDetails userDetails) {
        return pdf(reportService.studentProfile(id, userDetails));
    }

    @GetMapping("/students/{id}/curriculum-checklist")
    @PreAuthorize("hasAuthority('REPORT_GENERATE')")
    public ResponseEntity<byte[]> curriculumChecklist(@PathVariable UUID id, @AuthenticationPrincipal SisUserDetails userDetails) {
        return pdf(reportService.curriculumChecklist(id, userDetails));
    }

    @GetMapping("/enrollments/{id}/form")
    @PreAuthorize("hasAuthority('REPORT_GENERATE')")
    public ResponseEntity<byte[]> enrollmentForm(@PathVariable UUID id, @AuthenticationPrincipal SisUserDetails userDetails) {
        return pdf(reportService.enrollmentForm(id, userDetails));
    }

    @GetMapping("/assessments/{id}")
    @PreAuthorize("hasAuthority('REPORT_GENERATE')")
    public ResponseEntity<byte[]> assessmentForm(@PathVariable UUID id, @AuthenticationPrincipal SisUserDetails userDetails) {
        return pdf(reportService.assessmentForm(id, userDetails));
    }

    @GetMapping("/classes/{scheduleId}/class-list")
    @PreAuthorize("hasAuthority('REPORT_GENERATE')")
    public ResponseEntity<byte[]> classList(@PathVariable UUID scheduleId, @AuthenticationPrincipal SisUserDetails userDetails) {
        return pdf(reportService.classList(scheduleId, userDetails));
    }

    @GetMapping("/classes/{scheduleId}/grade-sheet")
    @PreAuthorize("hasAuthority('REPORT_GENERATE')")
    public ResponseEntity<byte[]> gradeSheet(@PathVariable UUID scheduleId, @AuthenticationPrincipal SisUserDetails userDetails) {
        return pdf(reportService.gradeSheet(scheduleId, userDetails));
    }

    @GetMapping("/students/{id}/grade-slip")
    @PreAuthorize("hasAuthority('REPORT_GENERATE')")
    public ResponseEntity<byte[]> gradeSlip(@PathVariable UUID id, @AuthenticationPrincipal SisUserDetails userDetails) {
        return pdf(reportService.gradeSlip(id, userDetails));
    }

    private ResponseEntity<byte[]> pdf(PdfReport report) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.inline().filename(report.filename()).build().toString())
                .body(report.bytes());
    }
}
