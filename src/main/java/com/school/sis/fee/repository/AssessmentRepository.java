package com.school.sis.fee.repository;

import com.school.sis.fee.entity.Assessment;
import com.school.sis.fee.entity.AssessmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;

public interface AssessmentRepository extends JpaRepository<Assessment, UUID>, JpaSpecificationExecutor<Assessment> {
    boolean existsByEnrollmentIdAndStatusNot(UUID enrollmentId, AssessmentStatus status);
    Optional<Assessment> findByEnrollmentIdAndStatusNot(UUID enrollmentId, AssessmentStatus status);
    boolean existsByEnrollmentIdAndStatusIn(UUID enrollmentId, Collection<AssessmentStatus> statuses);
}
