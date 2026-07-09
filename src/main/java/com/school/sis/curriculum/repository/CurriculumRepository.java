package com.school.sis.curriculum.repository;

import com.school.sis.curriculum.entity.Curriculum;
import com.school.sis.curriculum.entity.CurriculumStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CurriculumRepository extends JpaRepository<Curriculum, UUID> {
    Page<Curriculum> findByCurriculumCodeContainingIgnoreCaseOrCurriculumNameContainingIgnoreCase(String code, String name, Pageable pageable);
    List<Curriculum> findByProgramIdAndStatus(UUID programId, CurriculumStatus status);
}
