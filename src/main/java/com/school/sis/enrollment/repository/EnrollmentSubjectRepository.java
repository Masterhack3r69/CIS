package com.school.sis.enrollment.repository;

import com.school.sis.enrollment.entity.EnrollmentSubject;
import com.school.sis.enrollment.entity.EnrollmentSubjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentSubjectRepository extends JpaRepository<EnrollmentSubject, UUID> {
    Optional<EnrollmentSubject> findByIdAndEnrollmentId(UUID id, UUID enrollmentId);
    boolean existsByEnrollmentIdAndClassScheduleIdAndStatus(UUID enrollmentId, UUID classScheduleId, EnrollmentSubjectStatus status);
    List<EnrollmentSubject> findByEnrollmentIdOrderByCreatedAtAsc(UUID enrollmentId);
}
