package com.school.sis.setup.repository;

import com.school.sis.setup.entity.Faculty;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FacultyRepository extends JpaRepository<Faculty, UUID> {
    Page<Faculty> findByEmployeeNumberContainingIgnoreCaseOrFirstNameContainingIgnoreCaseOrLastNameContainingIgnoreCase(
            String employeeNumber,
            String firstName,
            String lastName,
            Pageable pageable
    );
}
