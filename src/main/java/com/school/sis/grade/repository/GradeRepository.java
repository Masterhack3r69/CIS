package com.school.sis.grade.repository;

import com.school.sis.grade.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface GradeRepository extends JpaRepository<Grade, UUID>, JpaSpecificationExecutor<Grade> {
    Optional<Grade> findByEnrollmentSubjectId(UUID enrollmentSubjectId);
    List<Grade> findByClassScheduleId(UUID classScheduleId);
    List<Grade> findByStudentIdOrderBySchoolYearSchoolYearAscSemesterSortOrderAscCourseCourseCodeAsc(UUID studentId);
}
