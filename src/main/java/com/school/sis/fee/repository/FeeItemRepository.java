package com.school.sis.fee.repository;

import com.school.sis.fee.entity.FeeItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FeeItemRepository extends JpaRepository<FeeItem, UUID> {
    Page<FeeItem> findByFeeCodeContainingIgnoreCaseOrFeeNameContainingIgnoreCase(String feeCode, String feeName, Pageable pageable);
}
