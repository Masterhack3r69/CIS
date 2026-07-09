package com.school.sis.grade.service;

import com.school.sis.auth.entity.User;
import com.school.sis.auth.repository.UserRepository;
import com.school.sis.auth.security.SisUserDetails;
import com.school.sis.common.exception.BusinessRuleException;
import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.response.PageResponse;
import com.school.sis.enrollment.entity.EnrollmentStatus;
import com.school.sis.enrollment.entity.EnrollmentSubject;
import com.school.sis.enrollment.entity.EnrollmentSubjectStatus;
import com.school.sis.enrollment.repository.EnrollmentSubjectRepository;
import com.school.sis.grade.dto.AcademicRecordResponse;
import com.school.sis.grade.dto.ClassGradeRowResponse;
import com.school.sis.grade.dto.ClassGradeSheetResponse;
import com.school.sis.grade.dto.GradeEncodeItemRequest;
import com.school.sis.grade.dto.GradeEncodeRequest;
import com.school.sis.grade.dto.GradeResponse;
import com.school.sis.grade.dto.GradeSearchCriteria;
import com.school.sis.grade.entity.AcademicRecord;
import com.school.sis.grade.entity.Grade;
import com.school.sis.grade.entity.GradeRemark;
import com.school.sis.grade.entity.GradeStatus;
import com.school.sis.grade.entity.GradeStatusHistory;
import com.school.sis.grade.entity.SpecialGrade;
import com.school.sis.grade.repository.AcademicRecordRepository;
import com.school.sis.grade.repository.GradeRepository;
import com.school.sis.grade.repository.GradeStatusHistoryRepository;
import com.school.sis.schedule.entity.ClassSchedule;
import com.school.sis.schedule.repository.ClassScheduleRepository;
import com.school.sis.setup.entity.Course;
import com.school.sis.setup.entity.Faculty;
import com.school.sis.student.entity.Student;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class GradeService {

    private static final BigDecimal MIN_GRADE = BigDecimal.valueOf(1.00);
    private static final BigDecimal MAX_GRADE = BigDecimal.valueOf(5.00);
    private static final BigDecimal PASSING_GRADE = BigDecimal.valueOf(3.00);
    private static final BigDecimal GRADE_INCREMENT_IN_HUNDREDTHS = BigDecimal.valueOf(25);

    private final GradeRepository gradeRepository;
    private final GradeStatusHistoryRepository statusHistoryRepository;
    private final AcademicRecordRepository academicRecordRepository;
    private final EnrollmentSubjectRepository enrollmentSubjectRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final UserRepository userRepository;

    public GradeService(
            GradeRepository gradeRepository,
            GradeStatusHistoryRepository statusHistoryRepository,
            AcademicRecordRepository academicRecordRepository,
            EnrollmentSubjectRepository enrollmentSubjectRepository,
            ClassScheduleRepository classScheduleRepository,
            UserRepository userRepository
    ) {
        this.gradeRepository = gradeRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.academicRecordRepository = academicRecordRepository;
        this.enrollmentSubjectRepository = enrollmentSubjectRepository;
        this.classScheduleRepository = classScheduleRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<GradeResponse> list(GradeSearchCriteria criteria, Pageable pageable) {
        return PageResponse.from(gradeRepository.findAll(specification(criteria), pageable).map(this::toGradeResponse));
    }

    @Transactional(readOnly = true)
    public ClassGradeSheetResponse classSheet(UUID scheduleId) {
        return toClassSheet(findSchedule(scheduleId), classSubjects(scheduleId), gradesBySubject(scheduleId));
    }

    @Transactional
    public ClassGradeSheetResponse encode(UUID scheduleId, GradeEncodeRequest request, SisUserDetails userDetails) {
        ClassSchedule schedule = findSchedule(scheduleId);
        User actor = findUser(userDetails);
        ensureAssignedFaculty(schedule, userDetails);

        Map<UUID, EnrollmentSubject> subjects = classSubjects(scheduleId).stream()
                .collect(Collectors.toMap(EnrollmentSubject::getId, Function.identity()));
        if (subjects.isEmpty()) {
            throw new BusinessRuleException("Class has no confirmed enrolled students");
        }

        for (GradeEncodeItemRequest item : request.grades()) {
            EnrollmentSubject subject = subjects.get(item.enrollmentSubjectId());
            if (subject == null) {
                throw new BusinessRuleException("Enrollment subject does not belong to this class");
            }
            Grade grade = gradeRepository.findByEnrollmentSubjectId(subject.getId()).orElseGet(Grade::new);
            boolean newGrade = grade.getId() == null;
            GradeStatus previousStatus = grade.getStatus();
            if (grade.getId() != null && grade.getStatus() != GradeStatus.ENCODED && grade.getStatus() != GradeStatus.RETURNED_FOR_CORRECTION) {
                throw new BusinessRuleException("Only encoded or returned grades can be updated");
            }
            applyGradeValue(grade, item);
            if (newGrade) {
                applyContext(grade, subject);
            }
            grade.setStatus(GradeStatus.ENCODED);
            grade.setEncodedBy(actor);
            grade.setEncodedAt(Instant.now());
            grade.setNotes(item.notes());
            Grade saved = gradeRepository.save(grade);
            recordHistory(saved, newGrade ? null : previousStatus, GradeStatus.ENCODED, actor, "Grade encoded");
        }
        return classSheet(scheduleId);
    }

    @Transactional
    public ClassGradeSheetResponse submit(UUID scheduleId, SisUserDetails userDetails) {
        ClassSchedule schedule = findSchedule(scheduleId);
        User actor = findUser(userDetails);
        ensureAssignedFaculty(schedule, userDetails);
        List<Grade> grades = completeClassGrades(scheduleId);
        for (Grade grade : grades) {
            ensureStatus(grade, GradeStatus.ENCODED, "Only encoded grades can be submitted");
            transition(grade, GradeStatus.SUBMITTED, actor, "Grade submitted");
            grade.setSubmittedBy(actor);
            grade.setSubmittedAt(Instant.now());
        }
        return classSheet(scheduleId);
    }

    @Transactional
    public ClassGradeSheetResponse review(UUID scheduleId, SisUserDetails userDetails) {
        User actor = findUser(userDetails);
        List<Grade> grades = completeClassGrades(scheduleId);
        for (Grade grade : grades) {
            ensureStatus(grade, GradeStatus.SUBMITTED, "Only submitted grades can be reviewed");
            transition(grade, GradeStatus.REVIEWED, actor, "Grade reviewed");
            grade.setReviewedBy(actor);
            grade.setReviewedAt(Instant.now());
        }
        return classSheet(scheduleId);
    }

    @Transactional
    public ClassGradeSheetResponse approve(UUID scheduleId, SisUserDetails userDetails) {
        User actor = findUser(userDetails);
        List<Grade> grades = completeClassGrades(scheduleId);
        for (Grade grade : grades) {
            ensureStatus(grade, GradeStatus.REVIEWED, "Only reviewed grades can be approved");
            transition(grade, GradeStatus.APPROVED, actor, "Grade approved");
            grade.setApprovedBy(actor);
            grade.setApprovedAt(Instant.now());
            upsertAcademicRecord(grade);
        }
        return classSheet(scheduleId);
    }

    @Transactional
    public ClassGradeSheetResponse lock(UUID scheduleId, SisUserDetails userDetails) {
        User actor = findUser(userDetails);
        List<Grade> grades = completeClassGrades(scheduleId);
        for (Grade grade : grades) {
            ensureStatus(grade, GradeStatus.APPROVED, "Only approved grades can be locked");
            transition(grade, GradeStatus.LOCKED, actor, "Grade locked");
            grade.setLockedAt(Instant.now());
            upsertAcademicRecord(grade);
        }
        return classSheet(scheduleId);
    }

    @Transactional(readOnly = true)
    public List<GradeResponse> studentGrades(UUID studentId) {
        return gradeRepository.findByStudentIdOrderBySchoolYearSchoolYearAscSemesterSortOrderAscCourseCourseCodeAsc(studentId)
                .stream()
                .map(this::toGradeResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AcademicRecordResponse> academicRecords(UUID studentId) {
        return academicRecordRepository.findByStudentIdOrderBySchoolYearSchoolYearAscSemesterSortOrderAscCourseCourseCodeAsc(studentId)
                .stream()
                .map(this::toAcademicRecordResponse)
                .toList();
    }

    private List<Grade> completeClassGrades(UUID scheduleId) {
        List<EnrollmentSubject> subjects = classSubjects(scheduleId);
        if (subjects.isEmpty()) {
            throw new BusinessRuleException("Class has no confirmed enrolled students");
        }
        Map<UUID, Grade> grades = gradesBySubject(scheduleId);
        if (grades.size() != subjects.size()) {
            throw new BusinessRuleException("All enrolled students must have encoded grades");
        }
        return subjects.stream()
                .map(subject -> grades.get(subject.getId()))
                .toList();
    }

    private void applyContext(Grade grade, EnrollmentSubject subject) {
        ClassSchedule schedule = subject.getClassSchedule();
        grade.setEnrollmentSubject(subject);
        grade.setStudent(subject.getEnrollment().getStudent());
        grade.setClassSchedule(schedule);
        grade.setCourse(schedule.getCourse());
        grade.setSection(schedule.getSection());
        grade.setFaculty(schedule.getFaculty());
        grade.setSchoolYear(schedule.getSchoolYear());
        grade.setSemester(schedule.getSemester());
    }

    private void applyGradeValue(Grade grade, GradeEncodeItemRequest request) {
        if ((request.finalGrade() == null && request.specialGrade() == null)
                || (request.finalGrade() != null && request.specialGrade() != null)) {
            throw new BusinessRuleException("Exactly one grade value is required");
        }
        if (request.finalGrade() != null) {
            validateNumericGrade(request.finalGrade());
            grade.setFinalGrade(request.finalGrade());
            grade.setSpecialGrade(null);
            grade.setRemark(request.finalGrade().compareTo(PASSING_GRADE) <= 0 ? GradeRemark.PASSED : GradeRemark.FAILED);
            return;
        }
        grade.setFinalGrade(null);
        grade.setSpecialGrade(request.specialGrade());
        grade.setRemark(specialRemark(request.specialGrade()));
    }

    private void validateNumericGrade(BigDecimal grade) {
        if (grade.compareTo(MIN_GRADE) < 0 || grade.compareTo(MAX_GRADE) > 0) {
            throw new BusinessRuleException("Numeric grade must be between 1.00 and 5.00");
        }
        BigDecimal hundredths = grade.movePointRight(2).stripTrailingZeros();
        if (hundredths.scale() > 0 || hundredths.remainder(GRADE_INCREMENT_IN_HUNDREDTHS).compareTo(BigDecimal.ZERO) != 0) {
            throw new BusinessRuleException("Numeric grade must use 0.25 increments");
        }
    }

    private GradeRemark specialRemark(SpecialGrade specialGrade) {
        return switch (specialGrade) {
            case INC -> GradeRemark.INCOMPLETE;
            case DRP -> GradeRemark.DROPPED;
            case NG -> GradeRemark.NO_GRADE;
            case W -> GradeRemark.WITHDRAWN;
            case COND -> GradeRemark.CONDITIONAL;
        };
    }

    private void ensureAssignedFaculty(ClassSchedule schedule, SisUserDetails userDetails) {
        if (userDetails == null || schedule.getFaculty().getUser() == null || !schedule.getFaculty().getUser().getId().equals(userDetails.id())) {
            throw new BusinessRuleException("Only the assigned faculty user can encode grades for this class");
        }
    }

    private void ensureStatus(Grade grade, GradeStatus expected, String message) {
        if (grade.getStatus() != expected) {
            throw new BusinessRuleException(message);
        }
    }

    private void transition(Grade grade, GradeStatus toStatus, User actor, String remarks) {
        GradeStatus fromStatus = grade.getStatus();
        grade.setStatus(toStatus);
        recordHistory(grade, fromStatus, toStatus, actor, remarks);
    }

    private void recordHistory(Grade grade, GradeStatus fromStatus, GradeStatus toStatus, User actor, String remarks) {
        GradeStatusHistory history = new GradeStatusHistory();
        history.setGrade(grade);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setChangedBy(actor);
        history.setRemarks(remarks);
        statusHistoryRepository.save(history);
    }

    private void upsertAcademicRecord(Grade grade) {
        AcademicRecord record = academicRecordRepository.findByGradeId(grade.getId()).orElseGet(AcademicRecord::new);
        record.setGrade(grade);
        record.setStudent(grade.getStudent());
        record.setProgram(grade.getStudent().getProgram());
        record.setCurriculum(grade.getStudent().getCurriculum());
        record.setSchoolYear(grade.getSchoolYear());
        record.setSemester(grade.getSemester());
        record.setCourse(grade.getCourse());
        record.setFaculty(grade.getFaculty());
        record.setFinalGrade(grade.getFinalGrade());
        record.setSpecialGrade(grade.getSpecialGrade());
        record.setRemark(grade.getRemark());
        record.setGradeStatus(grade.getStatus());
        record.setCreditUnits(grade.getCourse().getCreditUnits());
        record.setEarnedUnits(grade.getRemark() == GradeRemark.PASSED ? grade.getCourse().getCreditUnits() : BigDecimal.ZERO);
        record.setApprovedAt(grade.getApprovedAt());
        record.setLockedAt(grade.getLockedAt());
        academicRecordRepository.save(record);
    }

    private ClassSchedule findSchedule(UUID id) {
        return classScheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Schedule not found"));
    }

    private User findUser(SisUserDetails userDetails) {
        if (userDetails == null) {
            return null;
        }
        return userRepository.findById(userDetails.id()).orElse(null);
    }

    private List<EnrollmentSubject> classSubjects(UUID scheduleId) {
        return enrollmentSubjectRepository.findConfirmedActiveSubjectsBySchedule(
                scheduleId,
                EnrollmentSubjectStatus.ENROLLED,
                EnrollmentStatus.CONFIRMED
        );
    }

    private Map<UUID, Grade> gradesBySubject(UUID scheduleId) {
        return gradeRepository.findByClassScheduleId(scheduleId)
                .stream()
                .collect(Collectors.toMap(grade -> grade.getEnrollmentSubject().getId(), Function.identity()));
    }

    private Specification<Grade> specification(GradeSearchCriteria criteria) {
        return (root, query, cb) -> {
            query.distinct(true);
            if (criteria == null) {
                return cb.conjunction();
            }
            var predicate = cb.conjunction();
            if (criteria.schoolYearId() != null) predicate = cb.and(predicate, cb.equal(root.get("schoolYear").get("id"), criteria.schoolYearId()));
            if (criteria.semesterId() != null) predicate = cb.and(predicate, cb.equal(root.get("semester").get("id"), criteria.semesterId()));
            if (criteria.scheduleId() != null) predicate = cb.and(predicate, cb.equal(root.get("classSchedule").get("id"), criteria.scheduleId()));
            if (criteria.facultyId() != null) predicate = cb.and(predicate, cb.equal(root.get("faculty").get("id"), criteria.facultyId()));
            if (criteria.studentId() != null) predicate = cb.and(predicate, cb.equal(root.get("student").get("id"), criteria.studentId()));
            if (criteria.status() != null) predicate = cb.and(predicate, cb.equal(root.get("status"), criteria.status()));
            return predicate;
        };
    }

    private ClassGradeSheetResponse toClassSheet(ClassSchedule schedule, List<EnrollmentSubject> subjects, Map<UUID, Grade> grades) {
        return new ClassGradeSheetResponse(
                schedule.getId(),
                schedule.getCourse().getId(),
                schedule.getCourse().getCourseCode(),
                schedule.getCourse().getCourseTitle(),
                schedule.getSection().getId(),
                schedule.getSection().getSectionCode(),
                schedule.getFaculty().getId(),
                facultyName(schedule.getFaculty()),
                schedule.getSchoolYear().getId(),
                schedule.getSchoolYear().getSchoolYear(),
                schedule.getSemester().getId(),
                schedule.getSemester().getName(),
                subjects.size(),
                subjects.stream()
                        .sorted(Comparator.comparing(subject -> studentName(subject.getEnrollment().getStudent())))
                        .map(subject -> new ClassGradeRowResponse(
                                subject.getId(),
                                subject.getEnrollment().getStudent().getId(),
                                subject.getEnrollment().getStudent().getStudentNumber(),
                                studentName(subject.getEnrollment().getStudent()),
                                grades.get(subject.getId()) == null ? null : toGradeResponse(grades.get(subject.getId()))
                        ))
                        .toList()
        );
    }

    private GradeResponse toGradeResponse(Grade grade) {
        Student student = grade.getStudent();
        Faculty faculty = grade.getFaculty();
        Course course = grade.getCourse();
        return new GradeResponse(
                grade.getId(),
                grade.getEnrollmentSubject().getId(),
                student.getId(),
                student.getStudentNumber(),
                studentName(student),
                grade.getClassSchedule().getId(),
                course.getId(),
                course.getCourseCode(),
                course.getCourseTitle(),
                grade.getSection().getId(),
                grade.getSection().getSectionCode(),
                faculty.getId(),
                facultyName(faculty),
                grade.getSchoolYear().getId(),
                grade.getSchoolYear().getSchoolYear(),
                grade.getSemester().getId(),
                grade.getSemester().getName(),
                grade.getFinalGrade(),
                grade.getSpecialGrade(),
                grade.getRemark(),
                grade.getStatus(),
                grade.getNotes(),
                grade.getEncodedAt(),
                grade.getSubmittedAt(),
                grade.getReviewedAt(),
                grade.getApprovedAt(),
                grade.getLockedAt()
        );
    }

    public AcademicRecordResponse toAcademicRecordResponse(AcademicRecord record) {
        return new AcademicRecordResponse(
                record.getId(),
                record.getGrade().getId(),
                record.getSchoolYear().getId(),
                record.getSchoolYear().getSchoolYear(),
                record.getSemester().getId(),
                record.getSemester().getName(),
                record.getCourse().getId(),
                record.getCourse().getCourseCode(),
                record.getCourse().getCourseTitle(),
                record.getCreditUnits(),
                record.getEarnedUnits(),
                record.getFinalGrade(),
                record.getSpecialGrade(),
                record.getRemark(),
                record.getGradeStatus(),
                record.getFaculty().getId(),
                facultyName(record.getFaculty()),
                record.getApprovedAt(),
                record.getLockedAt()
        );
    }

    private String studentName(Student student) {
        return String.join(" ", List.of(student.getFirstName(), blankToEmpty(student.getMiddleName()), student.getLastName(), blankToEmpty(student.getSuffix())))
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String facultyName(Faculty faculty) {
        return String.join(" ", List.of(faculty.getFirstName(), blankToEmpty(faculty.getMiddleName()), faculty.getLastName(), blankToEmpty(faculty.getSuffix())))
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value;
    }
}
