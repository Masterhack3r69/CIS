package com.school.sis.grade.repository;

import com.school.sis.grade.entity.AcademicRecord;
import com.school.sis.grade.entity.GradeRemark;
import com.school.sis.grade.entity.GradeStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

public interface AcademicRecordRepository extends JpaRepository<AcademicRecord, UUID> {
    Optional<AcademicRecord> findByGradeId(UUID gradeId);
    List<AcademicRecord> findByStudentIdOrderBySchoolYearSchoolYearAscSemesterSortOrderAscCourseCourseCodeAsc(UUID studentId);

    @Query("""
            select record.course.id
            from AcademicRecord record
            where record.student.id = :studentId
              and record.remark = :remark
              and record.gradeStatus in :statuses
            """)
    Set<UUID> findCompletedCourseIds(
            @Param("studentId") UUID studentId,
            @Param("remark") GradeRemark remark,
            @Param("statuses") Collection<GradeStatus> statuses
    );
}
