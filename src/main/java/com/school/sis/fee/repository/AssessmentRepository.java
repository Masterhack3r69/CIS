package com.school.sis.fee.repository;

import com.school.sis.fee.entity.Assessment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID>, JpaSpecificationExecutor<Assessment> {
    boolean existsByEnrollmentId(UUID enrollmentId);
    Optional<Assessment> findByEnrollmentId(UUID enrollmentId);
}
