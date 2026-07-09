package com.school.sis.fee.repository;

import com.school.sis.fee.entity.FeeRule;
import com.school.sis.setup.entity.ActiveStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface FeeRuleRepository extends JpaRepository<FeeRule, UUID> {
    @Query("""
            select rule from FeeRule rule
            join fetch rule.feeItem item
            left join fetch rule.program
            left join fetch rule.schoolYear
            left join fetch rule.semester
            where rule.status = :status
              and item.status = :status
              and (rule.program is null or rule.program.id = :programId)
              and (rule.schoolYear is null or rule.schoolYear.id = :schoolYearId)
              and (rule.semester is null or rule.semester.id = :semesterId)
              and (rule.yearLevel is null or rule.yearLevel = :yearLevel)
            order by item.feeCode asc, rule.ruleType asc, rule.amount asc
            """)
    List<FeeRule> findApplicableRules(
            @Param("programId") UUID programId,
            @Param("schoolYearId") UUID schoolYearId,
            @Param("semesterId") UUID semesterId,
            @Param("yearLevel") Integer yearLevel,
            @Param("status") ActiveStatus status
    );
}
