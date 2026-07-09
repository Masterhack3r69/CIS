package com.school.sis.student.repository;

import com.school.sis.student.entity.StudentEducationalBackground;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentEducationalBackgroundRepository extends JpaRepository<StudentEducationalBackground, UUID> {
}
