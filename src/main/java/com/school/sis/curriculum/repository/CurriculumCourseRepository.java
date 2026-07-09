package com.school.sis.curriculum.repository;

import com.school.sis.curriculum.entity.CurriculumCourse;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CurriculumCourseRepository extends JpaRepository<CurriculumCourse, UUID> {
    boolean existsByCurriculumIdAndYearLevelAndSemesterIgnoreCaseAndCourseId(UUID curriculumId, int yearLevel, String semester, UUID courseId);
    Optional<CurriculumCourse> findByIdAndCurriculumId(UUID id, UUID curriculumId);
    List<CurriculumCourse> findByCurriculumIdOrderByYearLevelAscSemesterAscSortOrderAsc(UUID curriculumId);
}
