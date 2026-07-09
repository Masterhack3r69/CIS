package com.school.sis.enrollment.repository;

import com.school.sis.enrollment.entity.EnrollmentStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnrollmentStatusHistoryRepository extends JpaRepository<EnrollmentStatusHistory, UUID> {
    List<EnrollmentStatusHistory> findByEnrollmentIdOrderByChangedAtAsc(UUID enrollmentId);
}
