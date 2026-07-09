package com.school.sis.setup.repository;

import com.school.sis.setup.entity.Semester;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SemesterRepository extends JpaRepository<Semester, UUID> {
}
