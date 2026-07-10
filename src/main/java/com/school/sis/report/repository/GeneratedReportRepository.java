package com.school.sis.report.repository;

import com.school.sis.report.entity.GeneratedReport;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GeneratedReportRepository extends JpaRepository<GeneratedReport, UUID> {
    long countByReportTypeAndTargetEntityId(String reportType, UUID targetEntityId);
}
