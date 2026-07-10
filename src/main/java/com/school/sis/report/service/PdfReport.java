package com.school.sis.report.service;

public record PdfReport(
        String filename,
        byte[] bytes
) {
}
