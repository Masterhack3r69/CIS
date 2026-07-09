package com.school.sis.student.dto;

import com.school.sis.student.entity.DocumentVerificationStatus;

import java.time.Instant;
import java.util.UUID;

public record StudentDocumentResponse(
        UUID id,
        UUID studentId,
        String documentType,
        String fileName,
        String filePath,
        String mimeType,
        long fileSize,
        UUID uploadedBy,
        DocumentVerificationStatus verificationStatus,
        UUID verifiedBy,
        Instant verifiedAt,
        String remarks
) {
}
