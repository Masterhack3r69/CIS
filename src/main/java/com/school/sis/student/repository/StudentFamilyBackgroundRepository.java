package com.school.sis.student.repository;

import com.school.sis.student.entity.StudentFamilyBackground;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentFamilyBackgroundRepository extends JpaRepository<StudentFamilyBackground, UUID> {
}
