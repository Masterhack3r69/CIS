package com.school.sis.grade.repository;

import com.school.sis.grade.entity.AcademicRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, UUID> {
    Optional<AcademicRecord> findByGradeId(UUID gradeId);
    List<AcademicRecord> findByStudentIdOrderBySchoolYearSchoolYearAscSemesterSortOrderAscCourseCourseCodeAsc(UUID studentId);
}
