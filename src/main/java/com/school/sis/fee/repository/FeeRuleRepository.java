package com.school.sis.fee.repository;

import com.school.sis.fee.entity.FeeRule;
import com.school.sis.setup.entity.ActiveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FeeRuleRepository extends JpaRepository<FeeRule, UUID> {
    List<FeeRule> findByFeeItemIdOrderByCreatedAtAsc(UUID feeItemId);
    void deleteByFeeItemId(UUID feeItemId);

    @Query("""
            select rule from FeeRule rule
            join fetch rule.feeItem feeItem
            where rule.schoolYear.id = :schoolYearId
              and rule.status = :activeStatus
              and feeItem.status = :activeStatus
              and (rule.semester is null or rule.semester.id = :semesterId)
              and (rule.program is null or rule.program.id = :programId)
              and (rule.yearLevel is null or rule.yearLevel = :yearLevel)
            order by feeItem.feeCode asc
            """)
    List<FeeRule> findApplicableRules(
            @Param("schoolYearId") UUID schoolYearId,
            @Param("semesterId") UUID semesterId,
            @Param("programId") UUID programId,
            @Param("yearLevel") int yearLevel,
            @Param("activeStatus") ActiveStatus activeStatus
    );
}
