package com.school.sis.fee.service;

import com.school.sis.audit.service.AuditService;
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
import com.school.sis.fee.entity.Assessment;
import com.school.sis.fee.entity.AssessmentItem;
import com.school.sis.fee.entity.AssessmentStatus;
import com.school.sis.fee.entity.FeeCategory;
import com.school.sis.fee.entity.FeeRule;
import com.school.sis.fee.repository.AssessmentRepository;
import com.school.sis.fee.repository.FeeRuleRepository;
import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.student.entity.Student;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@Service
public class AssessmentService {

    private final AssessmentRepository assessmentRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final FeeRuleRepository feeRuleRepository;
    private final AuditService auditService;

    public AssessmentService(
            AssessmentRepository assessmentRepository,
            EnrollmentRepository enrollmentRepository,
            FeeRuleRepository feeRuleRepository,
            AuditService auditService
    ) {
        this.assessmentRepository = assessmentRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.feeRuleRepository = feeRuleRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<AssessmentSummaryResponse> list(AssessmentSearchCriteria criteria, Pageable pageable) {
        return PageResponse.from(assessmentRepository.findAll(specification(criteria), pageable).map(this::toSummary));
    }

    @Transactional(readOnly = true)
    public AssessmentResponse get(UUID id) {
        return toResponse(findAssessment(id));
    }

    @Transactional
    public AssessmentResponse generate(UUID enrollmentId) {
        if (assessmentRepository.existsByEnrollmentId(enrollmentId)) {
            throw new BusinessRuleException("Assessment already exists for this enrollment");
        }
        Enrollment enrollment = findEnrollment(enrollmentId);
        validateEnrollmentForAssessment(enrollment);

        Assessment assessment = new Assessment();
        assessment.setEnrollment(enrollment);
        assessment.setStudent(enrollment.getStudent());
        assessment.setSchoolYear(enrollment.getSchoolYear());
        assessment.setSemester(enrollment.getSemester());
        calculate(assessment, enrollment);
        Assessment saved = assessmentRepository.save(assessment);
        auditService.log("ASSESSMENT_GENERATED", "FEE", "Assessment", saved.getId(), null,
                Map.of("enrollmentId", enrollment.getId(), "studentId", saved.getStudent().getId(), "totalAssessment", saved.getTotalAssessment()));
        return toResponse(saved);
    }

    @Transactional
    public AssessmentResponse recalculate(UUID id) {
        Assessment assessment = findAssessment(id);
        if (assessment.getStatus() == AssessmentStatus.PAID
                || assessment.getStatus() == AssessmentStatus.CANCELLED
                || assessment.getStatus() == AssessmentStatus.REFUNDED
                || assessment.getAmountPaid().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessRuleException("Only unpaid active assessments can be recalculated");
        }
        validateEnrollmentForAssessment(assessment.getEnrollment());
        calculate(assessment, assessment.getEnrollment());
        auditService.log("ASSESSMENT_RECALCULATED", "FEE", "Assessment", assessment.getId(), null,
                Map.of("enrollmentId", assessment.getEnrollment().getId(), "totalAssessment", assessment.getTotalAssessment()));
        return toResponse(assessment);
    }

    @Transactional
    public AssessmentResponse updateStatus(UUID id, AssessmentStatusRequest request) {
        Assessment assessment = findAssessment(id);
        BigDecimal amountPaid = request.amountPaid() == null ? assessment.getAmountPaid() : request.amountPaid();
        if (amountPaid.compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessRuleException("Amount paid cannot be negative");
        }
        if (amountPaid.compareTo(assessment.getTotalAssessment()) > 0) {
            throw new BusinessRuleException("Amount paid cannot exceed total assessment");
        }
        AssessmentStatus oldStatus = assessment.getStatus();
        BigDecimal oldAmountPaid = assessment.getAmountPaid();
        assessment.setAmountPaid(amountPaid);
        assessment.setStatus(request.status());
        assessment.setBalance(assessment.getTotalAssessment().subtract(amountPaid));
        auditService.log("ASSESSMENT_STATUS_UPDATED", "FEE", "Assessment", assessment.getId(),
                Map.of("status", oldStatus.name(), "amountPaid", oldAmountPaid),
                Map.of("status", assessment.getStatus().name(), "amountPaid", assessment.getAmountPaid(), "balance", assessment.getBalance()));
        return toResponse(assessment);
    }

    private void calculate(Assessment assessment, Enrollment enrollment) {
        List<EnrollmentSubject> subjects = activeSubjects(enrollment);
        if (subjects.isEmpty()) {
            throw new BusinessRuleException("Assessment requires at least one enrolled subject");
        }
        List<FeeRule> rules = feeRuleRepository.findApplicableRules(
                enrollment.getSchoolYear().getId(),
                enrollment.getSemester().getId(),
                enrollment.getProgram().getId(),
                enrollment.getStudent().getYearLevel(),
                ActiveStatus.ACTIVE
        );
        BigDecimal totalUnits = totalUnits(subjects);
        assessment.setTotalUnits(totalUnits);
        assessment.setItems(rules.stream()
                .map(rule -> toAssessmentItem(rule, subjects, totalUnits))
                .filter(item -> item.getQuantity().compareTo(BigDecimal.ZERO) > 0)
                .toList());
        updateTotals(assessment);
    }

    private AssessmentItem toAssessmentItem(FeeRule rule, List<EnrollmentSubject> subjects, BigDecimal totalUnits) {
        BigDecimal quantity = quantity(rule, subjects, totalUnits);
        AssessmentItem item = new AssessmentItem();
        item.setFeeItem(rule.getFeeItem());
        item.setDescription(rule.getFeeItem().getFeeName());
        item.setCategory(rule.getFeeItem().getCategory());
        item.setComputationType(rule.getComputationType());
        item.setQuantity(quantity);
        item.setUnitAmount(rule.getAmount());
        item.setTotalAmount(rule.getAmount().multiply(quantity));
        return item;
    }

    private BigDecimal quantity(FeeRule rule, List<EnrollmentSubject> subjects, BigDecimal totalUnits) {
        return switch (rule.getComputationType()) {
            case PER_UNIT -> totalUnits;
            case PER_SUBJECT -> BigDecimal.valueOf(subjects.size());
            case PER_LABORATORY_SUBJECT -> BigDecimal.valueOf(subjects.stream()
                    .filter(subject -> subject.getClassSchedule().getCourse().getLaboratoryHoursPerWeek().compareTo(BigDecimal.ZERO) > 0)
                    .count());
            case FIXED_AMOUNT, PER_SEMESTER, PER_PROGRAM, PER_YEAR_LEVEL -> BigDecimal.ONE;
        };
    }

    private void updateTotals(Assessment assessment) {
        BigDecimal tuition = totalByCategory(assessment, FeeCategory.TUITION);
        BigDecimal laboratory = totalByCategory(assessment, FeeCategory.LABORATORY);
        BigDecimal miscellaneous = totalByCategory(assessment, FeeCategory.MISCELLANEOUS);
        BigDecimal other = totalByCategory(assessment, FeeCategory.OTHER);
        BigDecimal total = tuition.add(laboratory).add(miscellaneous).add(other)
                .add(assessment.getPenaltyAmount())
                .subtract(assessment.getDiscountAmount());
        assessment.setTuitionAmount(tuition);
        assessment.setLaboratoryFeeAmount(laboratory);
        assessment.setMiscellaneousFeeAmount(miscellaneous);
        assessment.setOtherFeeAmount(other);
        assessment.setTotalAssessment(total.max(BigDecimal.ZERO));
        assessment.setBalance(assessment.getTotalAssessment().subtract(assessment.getAmountPaid()).max(BigDecimal.ZERO));
        if (assessment.getAmountPaid().compareTo(BigDecimal.ZERO) == 0) {
            assessment.setStatus(AssessmentStatus.UNPAID);
        }
    }

    private BigDecimal totalByCategory(Assessment assessment, FeeCategory category) {
        return assessment.getItems().stream()
                .filter(item -> item.getCategory() == category)
                .map(AssessmentItem::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<EnrollmentSubject> activeSubjects(Enrollment enrollment) {
        return enrollment.getSubjects().stream()
                .filter(subject -> subject.getStatus() == EnrollmentSubjectStatus.ENROLLED)
                .toList();
    }

    private BigDecimal totalUnits(List<EnrollmentSubject> subjects) {
        return subjects.stream()
                .map(subject -> subject.getClassSchedule().getCourse().getCreditUnits())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void validateEnrollmentForAssessment(Enrollment enrollment) {
        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            throw new BusinessRuleException("Cancelled enrollments cannot be assessed");
        }
    }

    private Enrollment findEnrollment(UUID id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Enrollment not found"));
    }

    private Assessment findAssessment(UUID id) {
        return assessmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Assessment not found"));
    }

    private AssessmentResponse toResponse(Assessment assessment) {
        Student student = assessment.getStudent();
        return new AssessmentResponse(
                assessment.getId(),
                student.getId(),
                student.getStudentNumber(),
                studentName(student),
                assessment.getEnrollment().getId(),
                assessment.getSchoolYear().getId(),
                assessment.getSchoolYear().getSchoolYear(),
                assessment.getSemester().getId(),
                assessment.getSemester().getName(),
                assessment.getTotalUnits(),
                assessment.getTuitionAmount(),
                assessment.getLaboratoryFeeAmount(),
                assessment.getMiscellaneousFeeAmount(),
                assessment.getOtherFeeAmount(),
                assessment.getDiscountAmount(),
                assessment.getPenaltyAmount(),
                assessment.getTotalAssessment(),
                assessment.getAmountPaid(),
                assessment.getBalance(),
                assessment.getStatus(),
                assessment.getItems().stream()
                        .sorted(Comparator.comparing(AssessmentItem::getDescription))
                        .map(this::toItemResponse)
                        .toList()
        );
    }

    private AssessmentSummaryResponse toSummary(Assessment assessment) {
        Student student = assessment.getStudent();
        return new AssessmentSummaryResponse(
                assessment.getId(),
                student.getId(),
                student.getStudentNumber(),
                studentName(student),
                assessment.getEnrollment().getId(),
                assessment.getSchoolYear().getSchoolYear(),
                assessment.getSemester().getName(),
                assessment.getTotalAssessment(),
                assessment.getAmountPaid(),
                assessment.getBalance(),
                assessment.getStatus()
        );
    }

    private AssessmentItemResponse toItemResponse(AssessmentItem item) {
        return new AssessmentItemResponse(
                item.getId(),
                item.getFeeItem() == null ? null : item.getFeeItem().getId(),
                item.getFeeItem() == null ? null : item.getFeeItem().getFeeCode(),
                item.getEnrollmentSubject() == null ? null : item.getEnrollmentSubject().getId(),
                item.getDescription(),
                item.getCategory(),
                item.getComputationType(),
                item.getQuantity(),
                item.getUnitAmount(),
                item.getTotalAmount()
        );
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
                        cb.like(cb.lower(root.get("student").get("studentNumber")), term),
                        cb.like(cb.lower(root.get("student").get("firstName")), term),
                        cb.like(cb.lower(root.get("student").get("lastName")), term)
                ));
            }
            if (criteria.studentId() != null) predicate = cb.and(predicate, cb.equal(root.get("student").get("id"), criteria.studentId()));
            if (criteria.schoolYearId() != null) predicate = cb.and(predicate, cb.equal(root.get("schoolYear").get("id"), criteria.schoolYearId()));
            if (criteria.semesterId() != null) predicate = cb.and(predicate, cb.equal(root.get("semester").get("id"), criteria.semesterId()));
            if (criteria.status() != null) predicate = cb.and(predicate, cb.equal(root.get("status"), criteria.status()));
            return predicate;
        };
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
