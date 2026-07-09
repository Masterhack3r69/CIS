package com.school.sis.setup.repository;

import com.school.sis.setup.entity.Course;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CourseRepository extends JpaRepository<Course, UUID> {
    Page<Course> findByCourseCodeContainingIgnoreCaseOrCourseTitleContainingIgnoreCase(String code, String title, Pageable pageable);
}
