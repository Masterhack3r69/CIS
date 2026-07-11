package com.school.sis.enrollment.repository;

import com.school.sis.enrollment.entity.EnrollmentSubject;
import com.school.sis.enrollment.entity.EnrollmentSubjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface EnrollmentSubjectRepository extends JpaRepository<EnrollmentSubject, UUID> {
    Optional<EnrollmentSubject> findByIdAndEnrollmentId(UUID id, UUID enrollmentId);
    boolean existsByEnrollmentIdAndClassScheduleIdAndStatus(UUID enrollmentId, UUID classScheduleId, EnrollmentSubjectStatus status);
    List<EnrollmentSubject> findByEnrollmentIdOrderByCreatedAtAsc(UUID enrollmentId);
    long countByClassScheduleIdAndStatusAndEnrollmentStatus(UUID scheduleId, EnrollmentSubjectStatus subjectStatus,
                                                            com.school.sis.enrollment.entity.EnrollmentStatus enrollmentStatus);

    @Query("""
            select subject from EnrollmentSubject subject
            join fetch subject.enrollment enrollment
            join fetch enrollment.student
            join fetch subject.classSchedule schedule
            join fetch schedule.course
            join fetch schedule.section
            join fetch schedule.faculty
            join fetch schedule.schoolYear
            join fetch schedule.semester
            where schedule.id = :scheduleId
              and subject.status = com.school.sis.enrollment.entity.EnrollmentSubjectStatus.ENROLLED
              and enrollment.status = com.school.sis.enrollment.entity.EnrollmentStatus.CONFIRMED
            order by enrollment.student.lastName asc, enrollment.student.firstName asc
            """)
    List<EnrollmentSubject> findConfirmedEnrolledSubjectsByScheduleId(@Param("scheduleId") UUID scheduleId);
}
