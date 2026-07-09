package com.school.sis.setup.repository;

import com.school.sis.setup.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DepartmentRepository extends JpaRepository<Department, UUID> {
    Page<Department> findByDepartmentCodeContainingIgnoreCaseOrDepartmentNameContainingIgnoreCase(String code, String name, Pageable pageable);
}
