package com.school.sis.setup.repository;

import com.school.sis.setup.entity.SchoolYear;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SchoolYearRepository extends JpaRepository<SchoolYear, UUID> {
}
