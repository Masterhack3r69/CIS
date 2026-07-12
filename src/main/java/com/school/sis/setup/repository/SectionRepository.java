package com.school.sis.setup.repository;

import com.school.sis.setup.entity.Section;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SectionRepository extends JpaRepository<Section, UUID> {
    Page<Section> findBySectionCodeContainingIgnoreCase(String sectionCode, Pageable pageable);
    boolean existsBySectionCodeAndSchoolYearIdAndSemesterId(String sectionCode, java.util.UUID schoolYearId, java.util.UUID semesterId);
    boolean existsBySectionCodeAndSchoolYearIdAndSemesterIdAndIdNot(String sectionCode, java.util.UUID schoolYearId, java.util.UUID semesterId, java.util.UUID id);
    java.util.Optional<Section> findBySectionCodeAndSchoolYearIdAndSemesterId(String sectionCode, java.util.UUID schoolYearId, java.util.UUID semesterId);
}
