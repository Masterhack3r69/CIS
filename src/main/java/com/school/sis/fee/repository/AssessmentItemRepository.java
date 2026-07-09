package com.school.sis.fee.repository;

import com.school.sis.fee.entity.AssessmentItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface AssessmentItemRepository extends JpaRepository<AssessmentItem, UUID> {
}
