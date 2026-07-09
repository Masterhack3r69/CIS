package com.school.sis.fee.service;

import com.school.sis.common.exception.BusinessRuleException;
import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.response.PageResponse;
import com.school.sis.enrollment.entity.Enrollment;
import com.school.sis.enrollment.entity.EnrollmentStatus;
import com.school.sis.enrollment.entity.EnrollmentSubject;
import com.school.sis.enrollment.entity.EnrollmentSubjectStatus;
import com.school.sis.enrollment.repository.EnrollmentRepository;
import com.school.sis.fee.dto.AssessmentItemResponse;
import com.school.sis.fee.dto.AssessmentResponse;
import com.school.sis.fee.dto.AssessmentSearchCriteria;
import com.school.sis.fee.dto.AssessmentStatusRequest;
import com.school.sis.fee.dto.AssessmentSummaryResponse;
import com.school.sis.fee.dto.FeeRequest;
import com.school.sis.fee.dto.FeeResponse;
import com.school.sis.fee.dto.FeeRuleRequest;
import com.school.sis.fee.dto.FeeRuleResponse;
import com.school.sis.fee.entity.Assessment;
import com.school.sis.fee.entity.AssessmentItem;
import com.school.sis.fee.entity.AssessmentStatus;
import com.school.sis.fee.entity.FeeItem;
import com.school.sis.fee.entity.FeeRule;
import com.school.sis.fee.entity.FeeRuleType;
import com.school.sis.fee.repository.AssessmentRepository;
import com.school.sis.fee.repository.FeeItemRepository;
import com.school.sis.fee.repository.FeeRuleRepository;
import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.entity.Program;
import com.school.sis.setup.entity.SchoolYear;
import com.school.sis.setup.entity.Semester;
import com.school.sis.setup.repository.ProgramRepository;
import com.school.sis.setup.repository.SchoolYearRepository;
import com.school.sis.setup.repository.SemesterRepository;
import com.school.sis.student.entity.Student;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class FeeService {

    private final FeeItemRepository feeItemRepository;
    private final FeeRuleRepository feeRuleRepository;
    private final AssessmentRepository assessmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ProgramRepository programRepository;
    private final SchoolYearRepository schoolYearRepository;
    private final SemesterRepository semesterRepository;

    public FeeService(
            FeeItemRepository feeItemRepository,
            FeeRuleRepository feeRuleRepository,
            AssessmentRepository assessmentRepository,
            EnrollmentRepository enrollmentRepository,
            ProgramRepository programRepository,
            SchoolYearRepository schoolYearRepository,
            SemesterRepository semesterRepository
    ) {
        this.feeItemRepository = feeItemRepository;
        this.feeRuleRepository = feeRuleRepository;
        this.assessmentRepository = assessmentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.programRepository = programRepository;
        this.schoolYearRepository = schoolYearRepository;
        this.semesterRepository = semesterRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<FeeResponse> listFees(String search, Pageable pageable) {
        String term = search == null ? "" : search;
        return PageResponse.from(feeItemRepository
                .findByFeeCodeContainingIgnoreCaseOrFeeNameContainingIgnoreCase(term, term, pageable)
                .map(this::toFeeResponse));
    }

    @Transactional(readOnly = true)
    public FeeResponse getFee(UUID id) {
        return toFeeResponse(findFee(id));
    }

    @Transactional
    public FeeResponse createFee(FeeRequest request) {
        FeeItem feeItem = new FeeItem();
        applyFee(feeItem, request);
        return toFeeResponse(feeItemRepository.save(feeItem));
    }

    @Transactional
    public FeeResponse updateFee(UUID id, FeeRequest request) {
        FeeItem feeItem = findFee(id);
        applyFee(feeItem, request);
        return toFeeResponse(feeItem);
    }

    @Transactional
    public FeeResponse updateFeeStatus(UUID id, ActiveStatus status) {
        FeeItem feeItem = findFee(id);
        feeItem.setStatus(status);
        return toFeeResponse(feeItem);
    }

    @Transactional
    public AssessmentResponse generateAssessment(UUID enrollmentId) {
        Enrollment enrollment = findEnrollment(enrollmentId);
        if (enrollment.getStatus() != EnrollmentStatus.CONFIRMED) {
            throw new BusinessRuleException("Only confirmed enrollments can generate assessments");
        }
        if (assessmentRepository.existsByEnrollmentIdAndStatusNot(enrollmentId, AssessmentStatus.VOID)) {
            throw new BusinessRuleException("Enrollment already has an active assessment");
        }

        Assessment assessment = new Assessment();
        assessment.setAssessmentNumber(nextAssessmentNumber());
        assessment.setEnrollment(enrollment);
        assessment.setStudent(enrollment.getStudent());
        assessment.setSchoolYear(enrollment.getSchoolYear());
        assessment.setSemester(enrollment.getSemester());
        assessment.setStatus(AssessmentStatus.DRAFT);
        assessment.setRemarks("Assessment generated from confirmed enrollment");
        rebuildAssessmentItems(assessment, enrollment);
        return toAssessmentResponse(assessmentRepository.save(assessment));
    }

    @Transactional(readOnly = true)
    public PageResponse<AssessmentSummaryResponse> listAssessments(AssessmentSearchCriteria criteria, Pageable pageable) {
        return PageResponse.from(assessmentRepository.findAll(specification(criteria), pageable).map(this::toAssessmentSummary));
    }

    @Transactional(readOnly = true)
    public AssessmentResponse getAssessment(UUID id) {
        return toAssessmentResponse(findAssessment(id));
    }

    @Transactional
    public AssessmentResponse recalculateAssessment(UUID id) {
        Assessment assessment = findAssessment(id);
        if (assessment.getStatus() == AssessmentStatus.PAID || assessment.getStatus() == AssessmentStatus.VOID) {
            throw new BusinessRuleException("Paid or void assessments cannot be recalculated");
        }
        rebuildAssessmentItems(assessment, assessment.getEnrollment());
        assessment.setGeneratedAt(Instant.now());
        return toAssessmentResponse(assessment);
    }

    @Transactional
    public AssessmentResponse updateAssessmentStatus(UUID id, AssessmentStatusRequest request) {
        Assessment assessment = findAssessment(id);
        if (assessment.getStatus() == AssessmentStatus.VOID) {
            throw new BusinessRuleException("Void assessments cannot be updated");
        }
        if (assessment.getStatus() == AssessmentStatus.PAID && request.status() != AssessmentStatus.PAID) {
            throw new BusinessRuleException("Paid assessments cannot be changed");
        }
        assessment.setStatus(request.status());
        assessment.setRemarks(request.remarks());
        return toAssessmentResponse(assessment);
    }

    private void applyFee(FeeItem feeItem, FeeRequest request) {
        feeItem.setFeeCode(request.feeCode());
        feeItem.setFeeName(request.feeName());
        feeItem.setDescription(request.description());
        feeItem.setStatus(request.status() == null ? ActiveStatus.ACTIVE : request.status());
        feeItem.setRules(request.rules() == null ? List.of() : request.rules().stream().map(this::toRule).toList());
    }

    private FeeRule toRule(FeeRuleRequest request) {
        FeeRule rule = new FeeRule();
        rule.setRuleType(request.ruleType());
        rule.setAmount(request.amount());
        rule.setProgram(resolveProgram(request.programId()));
        rule.setSchoolYear(resolveSchoolYear(request.schoolYearId()));
        rule.setSemester(resolveSemester(request.semesterId()));
        rule.setYearLevel(request.yearLevel());
        rule.setStatus(request.status() == null ? ActiveStatus.ACTIVE : request.status());
        return rule;
    }

    private void rebuildAssessmentItems(Assessment assessment, Enrollment enrollment) {
        BigDecimal totalUnits = totalCreditUnits(enrollment);
        if (totalUnits.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessRuleException("Enrollment must have at least one active subject before assessment");
        }

        List<FeeRule> rules = feeRuleRepository.findApplicableRules(
                enrollment.getProgram().getId(),
                enrollment.getSchoolYear().getId(),
                enrollment.getSemester().getId(),
                enrollment.getStudent().getYearLevel(),
                ActiveStatus.ACTIVE
        );
        if (rules.isEmpty()) {
            throw new BusinessRuleException("No active fee rules found for enrollment");
        }

        assessment.setItems(List.of());
        int sortOrder = 1;
        for (FeeRule rule : rules) {
            BigDecimal quantity = rule.getRuleType() == FeeRuleType.PER_UNIT ? totalUnits : BigDecimal.ONE;
            BigDecimal lineAmount = quantity.multiply(rule.getAmount());

            AssessmentItem item = new AssessmentItem();
            item.setFeeItem(rule.getFeeItem());
            item.setFeeCode(rule.getFeeItem().getFeeCode());
            item.setFeeName(rule.getFeeItem().getFeeName());
            item.setRuleType(rule.getRuleType());
            item.setQuantity(quantity);
            item.setUnitAmount(rule.getAmount());
            item.setLineAmount(lineAmount);
            item.setSortOrder(sortOrder++);
            assessment.addItem(item);
        }

        BigDecimal subtotal = assessment.getItems().stream()
                .map(AssessmentItem::getLineAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        assessment.setTotalUnits(totalUnits);
        assessment.setSubtotalAmount(subtotal);
        assessment.setDiscountAmount(BigDecimal.ZERO);
        assessment.setTotalAmount(subtotal);
    }

    private BigDecimal totalCreditUnits(Enrollment enrollment) {
        return enrollment.getSubjects().stream()
                .filter(subject -> subject.getStatus() == EnrollmentSubjectStatus.ENROLLED)
                .map(EnrollmentSubject::getClassSchedule)
                .map(schedule -> schedule.getCourse().getCreditUnits())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String nextAssessmentNumber() {
        return "ASM-" + Instant.now().toEpochMilli() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(Locale.ROOT);
    }

    private FeeItem findFee(UUID id) {
        return feeItemRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Fee item not found"));
    }

    private Assessment findAssessment(UUID id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assessment not found"));
    }

    private Enrollment findEnrollment(UUID id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Enrollment not found"));
    }

    private Program resolveProgram(UUID id) {
        if (id == null) return null;
        return programRepository.findById(id).orElseThrow(() -> new NotFoundException("Program not found"));
    }

    private SchoolYear resolveSchoolYear(UUID id) {
        if (id == null) return null;
        return schoolYearRepository.findById(id).orElseThrow(() -> new NotFoundException("School year not found"));
    }

    private Semester resolveSemester(UUID id) {
        if (id == null) return null;
        return semesterRepository.findById(id).orElseThrow(() -> new NotFoundException("Semester not found"));
    }

    private Specification<Assessment> specification(AssessmentSearchCriteria criteria) {
        return (root, query, cb) -> {
            query.distinct(true);
            if (criteria == null) {
                return cb.conjunction();
            }
            var predicate = cb.conjunction();
            if (criteria.search() != null && !criteria.search().isBlank()) {
                String term = "%" + criteria.search().toLowerCase(Locale.ROOT) + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("assessmentNumber")), term),
                        cb.like(cb.lower(root.get("student").get("studentNumber")), term),
                        cb.like(cb.lower(root.get("student").get("firstName")), term),
                        cb.like(cb.lower(root.get("student").get("lastName")), term)
                ));
            }
            if (criteria.studentId() != null) predicate = cb.and(predicate, cb.equal(root.get("student").get("id"), criteria.studentId()));
            if (criteria.enrollmentId() != null) predicate = cb.and(predicate, cb.equal(root.get("enrollment").get("id"), criteria.enrollmentId()));
            if (criteria.schoolYearId() != null) predicate = cb.and(predicate, cb.equal(root.get("schoolYear").get("id"), criteria.schoolYearId()));
            if (criteria.semesterId() != null) predicate = cb.and(predicate, cb.equal(root.get("semester").get("id"), criteria.semesterId()));
            if (criteria.status() != null) predicate = cb.and(predicate, cb.equal(root.get("status"), criteria.status()));
            return predicate;
        };
    }

    private FeeResponse toFeeResponse(FeeItem feeItem) {
        return new FeeResponse(
                feeItem.getId(),
                feeItem.getFeeCode(),
                feeItem.getFeeName(),
                feeItem.getDescription(),
                feeItem.getStatus(),
                feeItem.getRules().stream()
                        .sorted(Comparator.comparing(FeeRule::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())))
                        .map(this::toRuleResponse)
                        .toList()
        );
    }

    private FeeRuleResponse toRuleResponse(FeeRule rule) {
        return new FeeRuleResponse(
                rule.getId(),
                rule.getRuleType(),
                rule.getAmount(),
                rule.getProgram() == null ? null : rule.getProgram().getId(),
                rule.getProgram() == null ? null : rule.getProgram().getProgramCode(),
                rule.getSchoolYear() == null ? null : rule.getSchoolYear().getId(),
                rule.getSchoolYear() == null ? null : rule.getSchoolYear().getSchoolYear(),
                rule.getSemester() == null ? null : rule.getSemester().getId(),
                rule.getSemester() == null ? null : rule.getSemester().getName(),
                rule.getYearLevel(),
                rule.getStatus()
        );
    }

    private AssessmentSummaryResponse toAssessmentSummary(Assessment assessment) {
        Student student = assessment.getStudent();
        return new AssessmentSummaryResponse(
                assessment.getId(),
                assessment.getAssessmentNumber(),
                assessment.getEnrollment().getId(),
                student.getId(),
                student.getStudentNumber(),
                studentName(student),
                assessment.getSchoolYear().getSchoolYear(),
                assessment.getSemester().getName(),
                assessment.getStatus(),
                assessment.getTotalUnits(),
                assessment.getTotalAmount()
        );
    }

    private AssessmentResponse toAssessmentResponse(Assessment assessment) {
        Student student = assessment.getStudent();
        return new AssessmentResponse(
                assessment.getId(),
                assessment.getAssessmentNumber(),
                assessment.getEnrollment().getId(),
                student.getId(),
                student.getStudentNumber(),
                studentName(student),
                assessment.getSchoolYear().getId(),
                assessment.getSchoolYear().getSchoolYear(),
                assessment.getSemester().getId(),
                assessment.getSemester().getName(),
                assessment.getStatus(),
                assessment.getTotalUnits(),
                assessment.getSubtotalAmount(),
                assessment.getDiscountAmount(),
                assessment.getTotalAmount(),
                assessment.getRemarks(),
                assessment.getGeneratedAt(),
                assessment.getItems().stream()
                        .sorted(Comparator.comparing(AssessmentItem::getSortOrder))
                        .map(this::toAssessmentItemResponse)
                        .toList()
        );
    }

    private AssessmentItemResponse toAssessmentItemResponse(AssessmentItem item) {
        return new AssessmentItemResponse(
                item.getId(),
                item.getFeeItem() == null ? null : item.getFeeItem().getId(),
                item.getFeeCode(),
                item.getFeeName(),
                item.getRuleType(),
                item.getQuantity(),
                item.getUnitAmount(),
                item.getLineAmount(),
                item.getSortOrder()
        );
    }

    private String studentName(Student student) {
        return String.join(" ", List.of(student.getFirstName(), blankToEmpty(student.getMiddleName()), student.getLastName(), blankToEmpty(student.getSuffix())))
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value;
    }
}
