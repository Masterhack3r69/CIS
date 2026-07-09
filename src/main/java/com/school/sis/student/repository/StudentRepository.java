package com.school.sis.student.repository;

import com.school.sis.student.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface StudentRepository extends JpaRepository<Student, UUID>, JpaSpecificationExecutor<Student> {
    boolean existsByStudentNumberIgnoreCase(String studentNumber);
    boolean existsByStudentNumberIgnoreCaseAndIdNot(String studentNumber, UUID id);
}
