package com.school.sis.setup.repository;

import com.school.sis.setup.entity.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SectionRepository extends JpaRepository<Section, UUID> {
    Page<Section> findBySectionCodeContainingIgnoreCase(String sectionCode, Pageable pageable);
}
