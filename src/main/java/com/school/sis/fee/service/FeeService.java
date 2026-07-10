package com.school.sis.fee.service;

import com.school.sis.audit.service.AuditService;
import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.response.PageResponse;
import com.school.sis.fee.dto.FeeItemRequest;
import com.school.sis.fee.dto.FeeItemResponse;
import com.school.sis.fee.dto.FeeItemSummaryResponse;
import com.school.sis.fee.dto.FeeRuleRequest;
import com.school.sis.fee.dto.FeeRuleResponse;
import com.school.sis.fee.dto.FeeSearchCriteria;
import com.school.sis.fee.entity.FeeItem;
import com.school.sis.fee.entity.FeeRule;
import com.school.sis.fee.repository.FeeItemRepository;
import com.school.sis.fee.repository.FeeRuleRepository;
import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.entity.Program;
import com.school.sis.setup.entity.SchoolYear;
import com.school.sis.setup.entity.Semester;
import com.school.sis.setup.repository.ProgramRepository;
import com.school.sis.setup.repository.SchoolYearRepository;
import com.school.sis.setup.repository.SemesterRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class FeeService {

    private final FeeItemRepository feeItemRepository;
    private final FeeRuleRepository feeRuleRepository;
    private final SchoolYearRepository schoolYearRepository;
    private final SemesterRepository semesterRepository;
    private final ProgramRepository programRepository;
    private final AuditService auditService;

    public FeeService(
            FeeItemRepository feeItemRepository,
            FeeRuleRepository feeRuleRepository,
            SchoolYearRepository schoolYearRepository,
            SemesterRepository semesterRepository,
            ProgramRepository programRepository,
            AuditService auditService
    ) {
        this.feeItemRepository = feeItemRepository;
        this.feeRuleRepository = feeRuleRepository;
        this.schoolYearRepository = schoolYearRepository;
        this.semesterRepository = semesterRepository;
        this.programRepository = programRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<FeeItemSummaryResponse> list(FeeSearchCriteria criteria, Pageable pageable) {
        return PageResponse.from(feeItemRepository.findAll(specification(criteria), pageable).map(this::toSummary));
    }

    @Transactional(readOnly = true)
    public FeeItemResponse get(UUID id) {
        return toResponse(findFeeItem(id));
    }

    @Transactional
    public FeeItemResponse create(FeeItemRequest request) {
        FeeItem feeItem = new FeeItem();
        apply(feeItem, request);
        FeeItem saved = feeItemRepository.save(feeItem);
        replaceRules(saved, request.rules());
        auditService.log("FEE_CREATED", "FEE", "FeeItem", saved.getId(), null,
                Map.of("feeCode", saved.getFeeCode(), "status", saved.getStatus().name()));
        return toResponse(saved);
    }

    @Transactional
    public FeeItemResponse update(UUID id, FeeItemRequest request) {
        FeeItem feeItem = findFeeItem(id);
        apply(feeItem, request);
        if (request.rules() != null) {
            replaceRules(feeItem, request.rules());
        }
        auditService.log("FEE_UPDATED", "FEE", "FeeItem", feeItem.getId(), null,
                Map.of("feeCode", feeItem.getFeeCode(), "status", feeItem.getStatus().name()));
        return toResponse(feeItem);
    }

    @Transactional
    public FeeItemResponse updateStatus(UUID id, ActiveStatus status) {
        FeeItem feeItem = findFeeItem(id);
        ActiveStatus oldStatus = feeItem.getStatus();
        feeItem.setStatus(status);
        auditService.log("FEE_STATUS_UPDATED", "FEE", "FeeItem", feeItem.getId(),
                Map.of("status", oldStatus.name()), Map.of("status", feeItem.getStatus().name()));
        return toResponse(feeItem);
    }

    private void apply(FeeItem feeItem, FeeItemRequest request) {
        feeItem.setFeeCode(request.feeCode());
        feeItem.setFeeName(request.feeName());
        feeItem.setCategory(request.category());
        feeItem.setDescription(request.description());
        feeItem.setStatus(request.status() == null ? ActiveStatus.ACTIVE : request.status());
    }

    private void replaceRules(FeeItem feeItem, List<FeeRuleRequest> requests) {
        feeRuleRepository.deleteByFeeItemId(feeItem.getId());
        if (requests == null || requests.isEmpty()) {
            return;
        }
        requests.forEach(request -> feeRuleRepository.save(toRule(feeItem, request)));
    }

    private FeeRule toRule(FeeItem feeItem, FeeRuleRequest request) {
        SchoolYear schoolYear = schoolYearRepository.findById(request.schoolYearId())
                .orElseThrow(() -> new NotFoundException("School year not found"));
        Semester semester = request.semesterId() == null ? null : semesterRepository.findById(request.semesterId())
                .orElseThrow(() -> new NotFoundException("Semester not found"));
        Program program = request.programId() == null ? null : programRepository.findById(request.programId())
                .orElseThrow(() -> new NotFoundException("Program not found"));

        FeeRule rule = new FeeRule();
        rule.setFeeItem(feeItem);
        rule.setSchoolYear(schoolYear);
        rule.setSemester(semester);
        rule.setProgram(program);
        rule.setYearLevel(request.yearLevel());
        rule.setComputationType(request.computationType());
        rule.setAmount(request.amount());
        rule.setStatus(request.status() == null ? ActiveStatus.ACTIVE : request.status());
        return rule;
    }

    private FeeItem findFeeItem(UUID id) {
        return feeItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fee item not found"));
    }

    private FeeItemResponse toResponse(FeeItem feeItem) {
        return new FeeItemResponse(
                feeItem.getId(),
                feeItem.getFeeCode(),
                feeItem.getFeeName(),
                feeItem.getCategory(),
                feeItem.getDescription(),
                feeItem.getStatus(),
                feeRuleRepository.findByFeeItemIdOrderByCreatedAtAsc(feeItem.getId()).stream()
                        .map(this::toRuleResponse)
                        .toList()
        );
    }

    private FeeItemSummaryResponse toSummary(FeeItem feeItem) {
        return new FeeItemSummaryResponse(feeItem.getId(), feeItem.getFeeCode(), feeItem.getFeeName(), feeItem.getCategory(), feeItem.getStatus());
    }

    private FeeRuleResponse toRuleResponse(FeeRule rule) {
        return new FeeRuleResponse(
                rule.getId(),
                rule.getSchoolYear().getId(),
                rule.getSchoolYear().getSchoolYear(),
                rule.getSemester() == null ? null : rule.getSemester().getId(),
                rule.getSemester() == null ? null : rule.getSemester().getName(),
                rule.getProgram() == null ? null : rule.getProgram().getId(),
                rule.getProgram() == null ? null : rule.getProgram().getProgramCode(),
                rule.getYearLevel(),
                rule.getComputationType(),
                rule.getAmount(),
                rule.getStatus()
        );
    }

    private Specification<FeeItem> specification(FeeSearchCriteria criteria) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (criteria == null) {
                return predicate;
            }
            if (criteria.search() != null && !criteria.search().isBlank()) {
                String term = "%" + criteria.search().toLowerCase(Locale.ROOT) + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("feeCode")), term),
                        cb.like(cb.lower(root.get("feeName")), term)
                ));
            }
            if (criteria.category() != null) predicate = cb.and(predicate, cb.equal(root.get("category"), criteria.category()));
            if (criteria.status() != null) predicate = cb.and(predicate, cb.equal(root.get("status"), criteria.status()));
            return predicate;
        };
    }
}
