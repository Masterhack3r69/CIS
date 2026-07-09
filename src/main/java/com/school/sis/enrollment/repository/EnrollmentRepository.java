package com.school.sis.enrollment.repository;

import com.school.sis.enrollment.entity.Enrollment;
import com.school.sis.enrollment.entity.EnrollmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.UUID;

public interface EnrollmentRepository extends JpaRepository<Enrollment, UUID>, JpaSpecificationExecutor<Enrollment> {
    boolean existsByStudentIdAndSchoolYearIdAndSemesterIdAndStatusIn(
            UUID studentId,
            UUID schoolYearId,
            UUID semesterId,
            Collection<EnrollmentStatus> statuses
    );
}
