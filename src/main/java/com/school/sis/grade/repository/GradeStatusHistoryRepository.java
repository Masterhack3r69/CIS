package com.school.sis.grade.repository;

import com.school.sis.grade.entity.GradeStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GradeStatusHistoryRepository extends JpaRepository<GradeStatusHistory, UUID> {
}
