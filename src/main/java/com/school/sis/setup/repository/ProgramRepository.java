package com.school.sis.setup.repository;

import com.school.sis.setup.entity.Program;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProgramRepository extends JpaRepository<Program, UUID> {
    Page<Program> findByProgramCodeContainingIgnoreCaseOrProgramNameContainingIgnoreCase(String code, String name, Pageable pageable);
}
