package com.school.sis.fee.repository;

import com.school.sis.fee.entity.FeeItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface FeeItemRepository extends JpaRepository<FeeItem, UUID>, JpaSpecificationExecutor<FeeItem> {
}
