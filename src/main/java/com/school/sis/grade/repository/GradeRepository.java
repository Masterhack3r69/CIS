package com.school.sis.grade.repository;

import com.school.sis.grade.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GradeRepository extends JpaRepository<Grade, UUID>, JpaSpecificationExecutor<Grade> {
    Optional<Grade> findByEnrollmentSubjectId(UUID enrollmentSubjectId);
    List<Grade> findByEnrollmentSubjectClassScheduleIdOrderByStudentLastNameAscStudentFirstNameAsc(UUID scheduleId);
    List<Grade> findByStudentIdOrderBySchoolYearSchoolYearAscSemesterSortOrderAscCourseCourseCodeAsc(UUID studentId);
    List<Grade> findByEnrollmentSubjectIdIn(Collection<UUID> enrollmentSubjectIds);
}
