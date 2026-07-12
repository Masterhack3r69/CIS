package com.school.sis.grade.service;

import com.school.sis.auth.entity.User;
import com.school.sis.auth.repository.UserRepository;
import com.school.sis.auth.security.SisUserDetails;
import com.school.sis.audit.service.AuditService;
import com.school.sis.common.exception.BusinessRuleException;
import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.response.PageResponse;
import com.school.sis.enrollment.entity.EnrollmentSubject;
import com.school.sis.enrollment.repository.EnrollmentSubjectRepository;
import com.school.sis.grade.dto.AcademicRecordResponse;
import com.school.sis.grade.dto.GradeClassResponse;
import com.school.sis.grade.dto.GradeEncodeRequest;
import com.school.sis.grade.dto.GradeEntryRequest;
import com.school.sis.grade.dto.GradeResponse;
import com.school.sis.grade.dto.GradeSearchCriteria;
import com.school.sis.grade.entity.AcademicRecord;
import com.school.sis.grade.entity.Grade;
import com.school.sis.grade.entity.GradeRemark;
import com.school.sis.grade.entity.GradeStatus;
import com.school.sis.grade.entity.GradeStatusHistory;
import com.school.sis.grade.repository.AcademicRecordRepository;
import com.school.sis.grade.repository.GradeRepository;
import com.school.sis.grade.repository.GradeStatusHistoryRepository;
import com.school.sis.schedule.entity.ClassSchedule;
import com.school.sis.schedule.repository.ClassScheduleRepository;
import com.school.sis.setup.entity.Faculty;
import com.school.sis.student.entity.Student;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GradeService {

    private static final Set<GradeStatus> EDITABLE_STATUSES = Set.of(GradeStatus.DRAFT, GradeStatus.ENCODED, GradeStatus.RETURNED_FOR_CORRECTION);
    private static final Set<GradeRemark> NON_NUMERIC_REMARKS = Set.of(
            GradeRemark.INCOMPLETE,
            GradeRemark.DROPPED,
            GradeRemark.NO_GRADE,
            GradeRemark.WITHDRAWN,
            GradeRemark.CONDITIONAL
    );
    private static final Set<GradeRemark> PASSING_REMARKS = Set.of(GradeRemark.PASSED);

    private final GradeRepository gradeRepository;
    private final GradeStatusHistoryRepository statusHistoryRepository;
    private final AcademicRecordRepository academicRecordRepository;
    private final EnrollmentSubjectRepository enrollmentSubjectRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public GradeService(
            GradeRepository gradeRepository,
            GradeStatusHistoryRepository statusHistoryRepository,
            AcademicRecordRepository academicRecordRepository,
            EnrollmentSubjectRepository enrollmentSubjectRepository,
            ClassScheduleRepository classScheduleRepository,
            UserRepository userRepository,
            AuditService auditService
    ) {
        this.gradeRepository = gradeRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.academicRecordRepository = academicRecordRepository;
        this.enrollmentSubjectRepository = enrollmentSubjectRepository;
        this.classScheduleRepository = classScheduleRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional(readOnly = true)
    public PageResponse<GradeResponse> list(GradeSearchCriteria criteria, Pageable pageable) {
        return PageResponse.from(gradeRepository.findAll(specification(criteria), pageable).map(this::toResponse));
    }

    @Transactional
    public GradeClassResponse classGrades(UUID scheduleId, SisUserDetails userDetails) {
        ClassSchedule schedule = findSchedule(scheduleId);
        ensureCanViewOrEncodeSchedule(schedule, userDetails);
        List<Grade> grades = ensureGrades(schedule);
        return toClassResponse(schedule, grades);
    }

    @Transactional
    public GradeClassResponse encode(UUID scheduleId, GradeEncodeRequest request, SisUserDetails userDetails) {
        ClassSchedule schedule = findSchedule(scheduleId);
        ensureCanEncodeSchedule(schedule, userDetails);
        List<Grade> grades = ensureGrades(schedule);
        Map<UUID, Grade> bySubjectId = grades.stream()
                .collect(Collectors.toMap(grade -> grade.getEnrollmentSubject().getId(), Function.identity()));
        User user = currentUser(userDetails);
        for (GradeEntryRequest entry : request.grades()) {
            Grade grade = bySubjectId.get(entry.enrollmentSubjectId());
            if (grade == null) {
                throw new BusinessRuleException("Enrollment subject does not belong to this class");
            }
            ensureEditable(grade);
            applyGradeEntry(grade, entry, user);
        }
        auditService.log(user, "GRADE_ENCODED", "GRADE", "ClassSchedule", schedule.getId(), null,
                Map.of("encodedCount", request.grades().size(), "courseId", schedule.getCourse().getId()));
        return toClassResponse(schedule, grades);
    }

    @Transactional
    public GradeClassResponse submit(UUID scheduleId, SisUserDetails userDetails) {
        ClassSchedule schedule = findSchedule(scheduleId);
        ensureCanEncodeSchedule(schedule, userDetails);
        List<Grade> grades = ensureGrades(schedule);
        if (grades.isEmpty()) {
            throw new BusinessRuleException("Class has no confirmed enrolled students");
        }
        User user = currentUser(userDetails);
        for (Grade grade : grades) {
            if (!isCompleteForSubmit(grade)) {
                throw new BusinessRuleException("All class grades must be encoded before submission");
            }
            if (grade.getStatus() == GradeStatus.LOCKED || grade.getStatus() == GradeStatus.APPROVED) {
                throw new BusinessRuleException("Approved or locked grades cannot be submitted");
            }
            changeStatus(grade, GradeStatus.SUBMITTED, "Grade submitted", user);
            grade.setSubmittedBy(user);
            grade.setSubmittedAt(Instant.now());
        }
        auditService.log(user, "GRADE_SUBMITTED", "GRADE", "ClassSchedule", schedule.getId(), null,
                Map.of("gradeCount", grades.size(), "status", GradeStatus.SUBMITTED.name()));
        return toClassResponse(schedule, grades);
    }

    @Transactional
    public GradeClassResponse approve(UUID scheduleId, SisUserDetails userDetails) {
        ClassSchedule schedule = findSchedule(scheduleId);
        ensureApprover(userDetails);
        List<Grade> grades = currentClassGrades(schedule);
        if (grades.isEmpty()) {
            throw new BusinessRuleException("Class has no submitted grades");
        }
        User user = currentUser(userDetails);
        for (Grade grade : grades) {
            if (grade.getStatus() != GradeStatus.SUBMITTED) {
                throw new BusinessRuleException("Only submitted grades can be approved");
            }
            changeStatus(grade, GradeStatus.APPROVED, "Grade approved", user);
            grade.setApprovedBy(user);
            grade.setApprovedAt(Instant.now());
        }
        auditService.log(user, "GRADE_APPROVED", "GRADE", "ClassSchedule", schedule.getId(), null,
                Map.of("gradeCount", grades.size(), "status", GradeStatus.APPROVED.name()));
        return toClassResponse(schedule, grades);
    }

    @Transactional
    public GradeClassResponse lock(UUID scheduleId, SisUserDetails userDetails) {
        ClassSchedule schedule = findSchedule(scheduleId);
        ensureApprover(userDetails);
        List<Grade> grades = currentClassGrades(schedule);
        if (grades.isEmpty()) {
            throw new BusinessRuleException("Class has no approved grades");
        }
        User user = currentUser(userDetails);
        for (Grade grade : grades) {
            if (grade.getStatus() != GradeStatus.APPROVED) {
                throw new BusinessRuleException("Only approved grades can be locked");
            }
            changeStatus(grade, GradeStatus.LOCKED, "Grade locked", user);
            grade.setLockedAt(Instant.now());
            upsertAcademicRecord(grade);
        }
        auditService.log(user, "GRADE_LOCKED", "GRADE", "ClassSchedule", schedule.getId(), null,
                Map.of("gradeCount", grades.size(), "status", GradeStatus.LOCKED.name()));
        return toClassResponse(schedule, grades);
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> studentGrades(UUID studentId) {
        return gradeRepository.findByStudentIdOrderBySchoolYearSchoolYearAscSemesterSortOrderAscCourseCourseCodeAsc(studentId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AcademicRecordResponse> academicRecords(UUID studentId) {
        return academicRecordRepository.findByStudentIdOrderBySchoolYearSchoolYearAscSemesterSortOrderAscCourseCodeAsc(studentId)
                .stream()
                .map(this::toAcademicRecordResponse)
                .toList();
    }

    public boolean hasPassedLockedCourse(UUID studentId, UUID courseId) {
        return academicRecordRepository.existsByStudentIdAndCourseIdAndGradeStatusAndRemarksIn(
                studentId,
                courseId,
                GradeStatus.LOCKED,
                PASSING_REMARKS
        );
    }

    private List<Grade> ensureGrades(ClassSchedule schedule) {
        List<EnrollmentSubject> subjects = enrollmentSubjectRepository.findConfirmedEnrolledSubjectsByScheduleId(schedule.getId());
        Map<UUID, Grade> existing = gradeRepository.findByEnrollmentSubjectIdIn(subjects.stream().map(EnrollmentSubject::getId).toList())
                .stream()
                .collect(Collectors.toMap(grade -> grade.getEnrollmentSubject().getId(), Function.identity()));
        return subjects.stream()
                .map(subject -> existing.computeIfAbsent(subject.getId(), ignored -> gradeRepository.save(newDraftGrade(subject))))
                .sorted(Comparator.comparing(grade -> grade.getStudent().getLastName() + grade.getStudent().getFirstName()))
                .toList();
    }

    private List<Grade> currentClassGrades(ClassSchedule schedule) {
        return gradeRepository.findByEnrollmentSubjectClassScheduleIdOrderByStudentLastNameAscStudentFirstNameAsc(schedule.getId());
    }

    private Grade newDraftGrade(EnrollmentSubject subject) {
        ClassSchedule schedule = subject.getClassSchedule();
        Grade grade = new Grade();
        grade.setEnrollmentSubject(subject);
        grade.setStudent(subject.getEnrollment().getStudent());
        grade.setCourse(schedule.getCourse());
        grade.setSection(schedule.getSection());
        grade.setFaculty(schedule.getFaculty());
        grade.setSchoolYear(schedule.getSchoolYear());
        grade.setSemester(schedule.getSemester());
        grade.setStatus(GradeStatus.DRAFT);
        grade.setRemarks(GradeRemark.NO_GRADE);
        return grade;
    }

    private void applyGradeEntry(Grade grade, GradeEntryRequest entry, User user) {
        validateGradeEntry(entry);
        grade.setFinalGrade(entry.finalGrade());
        grade.setRemarks(resolveRemark(entry));
        grade.setEncodedBy(user);
        grade.setEncodedAt(Instant.now());
        if (grade.getStatus() != GradeStatus.ENCODED) {
            changeStatus(grade, GradeStatus.ENCODED, "Grade encoded", user);
        }
    }

    private void validateGradeEntry(GradeEntryRequest entry) {
        if (entry.finalGrade() == null) {
            if (entry.remarks() == null || !NON_NUMERIC_REMARKS.contains(entry.remarks())) {
                throw new BusinessRuleException("A non-numeric grade requires an explicit non-numeric remark");
            }
            return;
        }
        BigDecimal normalized = entry.finalGrade().stripTrailingZeros();
        if (entry.finalGrade().compareTo(new BigDecimal("1.00")) < 0 || entry.finalGrade().compareTo(new BigDecimal("5.00")) > 0) {
            throw new BusinessRuleException("Final grade must be between 1.00 and 5.00");
        }
        BigDecimal multiplied = normalized.multiply(BigDecimal.valueOf(4));
        if (multiplied.remainder(BigDecimal.ONE).compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessRuleException("Final grade must use 0.25 increments");
        }
    }

    private GradeRemark resolveRemark(GradeEntryRequest entry) {
        if (entry.finalGrade() == null) {
            return entry.remarks();
        }
        return entry.finalGrade().compareTo(new BigDecimal("3.00")) <= 0 ? GradeRemark.PASSED : GradeRemark.FAILED;
    }

    private boolean isCompleteForSubmit(Grade grade) {
        return grade.getStatus() == GradeStatus.ENCODED
                && (grade.getFinalGrade() != null || NON_NUMERIC_REMARKS.contains(grade.getRemarks()));
    }

    private void ensureEditable(Grade grade) {
        if (!EDITABLE_STATUSES.contains(grade.getStatus())) {
            throw new BusinessRuleException("Submitted, approved, or locked grades cannot be edited");
        }
    }

    private void changeStatus(Grade grade, GradeStatus toStatus, String remarks, User user) {
        GradeStatus fromStatus = grade.getStatus();
        grade.setStatus(toStatus);
        GradeStatusHistory history = new GradeStatusHistory();
        history.setGrade(grade);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setRemarks(remarks);
        history.setChangedBy(user);
        history.setChangedAt(Instant.now());
        statusHistoryRepository.save(history);
    }

    private void upsertAcademicRecord(Grade grade) {
        AcademicRecord record = academicRecordRepository.findByGradeId(grade.getId()).orElseGet(AcademicRecord::new);
        record.setGrade(grade);
        record.setStudent(grade.getStudent());
        record.setProgram(grade.getStudent().getProgram());
        record.setCurriculum(grade.getStudent().getCurriculum());
        record.setCourse(grade.getCourse());
        record.setSection(grade.getSection());
        record.setFaculty(grade.getFaculty());
        record.setSchoolYear(grade.getSchoolYear());
        record.setSemester(grade.getSemester());
        record.setCourseCode(grade.getCourse().getCourseCode());
        record.setCourseTitle(grade.getCourse().getCourseTitle());
        record.setCreditUnits(grade.getCourse().getCreditUnits());
        record.setFinalGrade(grade.getFinalGrade());
        record.setRemarks(grade.getRemarks());
        record.setGradeStatus(grade.getStatus());
        record.setEarnedUnits(grade.getRemarks() == GradeRemark.PASSED ? grade.getCourse().getCreditUnits() : BigDecimal.ZERO);
        record.setApprovedAt(grade.getApprovedAt());
        record.setLockedAt(grade.getLockedAt());
        academicRecordRepository.save(record);
    }

    private void ensureCanViewOrEncodeSchedule(ClassSchedule schedule, SisUserDetails userDetails) {
        if (hasAuthority(userDetails, "GRADE_APPROVE")) {
            return;
        }
        ensureCanEncodeSchedule(schedule, userDetails);
    }

    private void ensureCanEncodeSchedule(ClassSchedule schedule, SisUserDetails userDetails) {
        if (hasAuthority(userDetails, "GRADE_APPROVE")) {
            return;
        }
        if (!hasAuthority(userDetails, "GRADE_ENCODE")) {
            throw new BusinessRuleException("User is not allowed to encode grades");
        }
        UUID facultyId = userDetails == null ? null : userDetails.facultyId();
        if (facultyId == null || !facultyId.equals(schedule.getFaculty().getId())) {
            throw new BusinessRuleException("Faculty can only encode assigned classes");
        }
    }

    private void ensureApprover(SisUserDetails userDetails) {
        if (!hasAuthority(userDetails, "GRADE_APPROVE")) {
            throw new BusinessRuleException("User is not allowed to approve grades");
        }
    }

    private boolean hasAuthority(SisUserDetails userDetails, String authority) {
        return userDetails != null && userDetails.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    private User currentUser(SisUserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userRepository.findById(userDetails.id()).orElse(null);
    }

    private ClassSchedule findSchedule(UUID id) {
        return classScheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Schedule not found"));
    }

    private GradeClassResponse toClassResponse(ClassSchedule schedule, List<Grade> grades) {
        return new GradeClassResponse(
                schedule.getId(),
                schedule.getCourse().getId(),
                schedule.getCourse().getCourseCode(),
                schedule.getCourse().getCourseTitle(),
                schedule.getSection().getId(),
                schedule.getSection().getSectionCode(),
                schedule.getFaculty().getId(),
                facultyName(schedule.getFaculty()),
                grades.stream().map(this::toResponse).toList()
        );
    }

    private GradeResponse toResponse(Grade grade) {
        Student student = grade.getStudent();
        return new GradeResponse(
                grade.getId(),
                grade.getEnrollmentSubject().getId(),
                student.getId(),
                student.getStudentNumber(),
                studentName(student),
                grade.getCourse().getId(),
                grade.getCourse().getCourseCode(),
                grade.getCourse().getCourseTitle(),
                grade.getSection().getId(),
                grade.getSection().getSectionCode(),
                grade.getFaculty().getId(),
                facultyName(grade.getFaculty()),
                grade.getSchoolYear().getId(),
                grade.getSchoolYear().getSchoolYear(),
                grade.getSemester().getId(),
                grade.getSemester().getName(),
                grade.getFinalGrade(),
                grade.getRemarks(),
                grade.getStatus(),
                grade.getEncodedAt(),
                grade.getSubmittedAt(),
                grade.getApprovedAt(),
                grade.getLockedAt()
        );
    }

    private AcademicRecordResponse toAcademicRecordResponse(AcademicRecord record) {
        return new AcademicRecordResponse(
                record.getId(),
                record.getGrade().getId(),
                record.getCourse().getId(),
                record.getCourseCode(),
                record.getCourseTitle(),
                record.getCreditUnits(),
                record.getEarnedUnits(),
                record.getFinalGrade(),
                record.getRemarks(),
                record.getGradeStatus(),
                record.getSection().getId(),
                record.getSection().getSectionCode(),
                record.getFaculty().getId(),
                facultyName(record.getFaculty()),
                record.getSchoolYear().getId(),
                record.getSchoolYear().getSchoolYear(),
                record.getSemester().getId(),
                record.getSemester().getName(),
                record.getApprovedAt(),
                record.getLockedAt()
        );
    }

    private Specification<Grade> specification(GradeSearchCriteria criteria) {
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
                        cb.like(cb.lower(root.get("student").get("lastName")), term),
                        cb.like(cb.lower(root.get("course").get("courseCode")), term),
                        cb.like(cb.lower(root.get("course").get("courseTitle")), term)
                ));
            }
            if (criteria.studentId() != null) predicate = cb.and(predicate, cb.equal(root.get("student").get("id"), criteria.studentId()));
            if (criteria.courseId() != null) predicate = cb.and(predicate, cb.equal(root.get("course").get("id"), criteria.courseId()));
            if (criteria.sectionId() != null) predicate = cb.and(predicate, cb.equal(root.get("section").get("id"), criteria.sectionId()));
            if (criteria.facultyId() != null) predicate = cb.and(predicate, cb.equal(root.get("faculty").get("id"), criteria.facultyId()));
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

    private String facultyName(Faculty faculty) {
        if (faculty == null) return null;
        String middleInitial = faculty.getMiddleName() != null && !faculty.getMiddleName().isBlank() ? faculty.getMiddleName().substring(0, 1).toUpperCase() + "." : "";
        return String.join(" ", List.of(faculty.getFirstName(), middleInitial, faculty.getLastName(), blankToEmpty(faculty.getSuffix())))
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value;
    }
}
