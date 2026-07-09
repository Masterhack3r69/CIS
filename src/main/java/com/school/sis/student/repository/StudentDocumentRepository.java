package com.school.sis.student.repository;

import com.school.sis.student.entity.DocumentVerificationStatus;
import com.school.sis.student.entity.StudentDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StudentDocumentRepository extends JpaRepository<StudentDocument, UUID> {
    List<StudentDocument> findByStudentIdOrderByCreatedAtDesc(UUID studentId);
    Optional<StudentDocument> findByIdAndStudentId(UUID id, UUID studentId);
    boolean existsByStudentIdAndVerificationStatus(UUID studentId, DocumentVerificationStatus status);
}
