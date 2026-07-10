package com.school.sis.grade.repository;

import com.school.sis.grade.entity.AcademicRecord;
import com.school.sis.grade.entity.GradeRemark;
import com.school.sis.grade.entity.GradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, UUID> {
    Optional<AcademicRecord> findByGradeId(UUID gradeId);
    List<AcademicRecord> findByStudentIdOrderBySchoolYearSchoolYearAscSemesterSortOrderAscCourseCodeAsc(UUID studentId);
    boolean existsByStudentIdAndCourseIdAndGradeStatusAndRemarksIn(UUID studentId, UUID courseId, GradeStatus gradeStatus, Collection<GradeRemark> remarks);
}
