package com.school.sis.enrollment.service;

import com.school.sis.common.exception.BusinessRuleException;
import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.response.PageResponse;
import com.school.sis.curriculum.entity.CurriculumCourse;
import com.school.sis.curriculum.repository.CurriculumCourseRepository;
import com.school.sis.enrollment.dto.EnrollmentRequest;
import com.school.sis.enrollment.dto.EnrollmentResponse;
import com.school.sis.enrollment.dto.EnrollmentSearchCriteria;
import com.school.sis.enrollment.dto.EnrollmentSubjectRequest;
import com.school.sis.enrollment.dto.EnrollmentSubjectResponse;
import com.school.sis.enrollment.dto.EnrollmentSummaryResponse;
import com.school.sis.enrollment.dto.EnrollmentUpdateRequest;
import com.school.sis.enrollment.dto.EnrollmentValidationIssueResponse;
import com.school.sis.enrollment.dto.EnrollmentValidationResponse;
import com.school.sis.enrollment.entity.Enrollment;
import com.school.sis.enrollment.entity.EnrollmentStatus;
import com.school.sis.enrollment.entity.EnrollmentStatusHistory;
import com.school.sis.enrollment.entity.EnrollmentSubject;
import com.school.sis.enrollment.entity.EnrollmentSubjectStatus;
import com.school.sis.enrollment.repository.EnrollmentRepository;
import com.school.sis.enrollment.repository.EnrollmentStatusHistoryRepository;
import com.school.sis.enrollment.repository.EnrollmentSubjectRepository;
import com.school.sis.grade.entity.GradeRemark;
import com.school.sis.grade.entity.GradeStatus;
import com.school.sis.grade.repository.AcademicRecordRepository;
import com.school.sis.schedule.dto.ScheduleMeetingResponse;
import com.school.sis.schedule.entity.ClassSchedule;
import com.school.sis.schedule.entity.ScheduleMeeting;
import com.school.sis.schedule.entity.ScheduleStatus;
import com.school.sis.schedule.repository.ClassScheduleRepository;
import com.school.sis.setup.entity.Course;
import com.school.sis.setup.entity.Faculty;
import com.school.sis.setup.entity.SchoolYear;
import com.school.sis.setup.entity.Section;
import com.school.sis.setup.entity.Semester;
import com.school.sis.setup.repository.SchoolYearRepository;
import com.school.sis.setup.repository.SectionRepository;
import com.school.sis.setup.repository.SemesterRepository;
import com.school.sis.student.entity.Student;
import com.school.sis.student.repository.StudentRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.function.Function;

@Service
public class EnrollmentService {

    private static final Set<EnrollmentStatus> ACTIVE_ENROLLMENT_STATUSES = Set.of(EnrollmentStatus.DRAFT, EnrollmentStatus.CONFIRMED);
    private static final Set<GradeStatus> COMPLETED_GRADE_STATUSES = Set.of(GradeStatus.APPROVED, GradeStatus.LOCKED);

    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentSubjectRepository enrollmentSubjectRepository;
    private final EnrollmentStatusHistoryRepository statusHistoryRepository;
    private final StudentRepository studentRepository;
    private final SchoolYearRepository schoolYearRepository;
    private final SemesterRepository semesterRepository;
    private final SectionRepository sectionRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final CurriculumCourseRepository curriculumCourseRepository;
    private final AcademicRecordRepository academicRecordRepository;

    public EnrollmentService(
            EnrollmentRepository enrollmentRepository,
            EnrollmentSubjectRepository enrollmentSubjectRepository,
            EnrollmentStatusHistoryRepository statusHistoryRepository,
            StudentRepository studentRepository,
            SchoolYearRepository schoolYearRepository,
            SemesterRepository semesterRepository,
            SectionRepository sectionRepository,
            ClassScheduleRepository classScheduleRepository,
            CurriculumCourseRepository curriculumCourseRepository,
            AcademicRecordRepository academicRecordRepository
    ) {
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentSubjectRepository = enrollmentSubjectRepository;
        this.statusHistoryRepository = statusHistoryRepository;
        this.studentRepository = studentRepository;
        this.schoolYearRepository = schoolYearRepository;
        this.semesterRepository = semesterRepository;
        this.sectionRepository = sectionRepository;
        this.classScheduleRepository = classScheduleRepository;
        this.curriculumCourseRepository = curriculumCourseRepository;
        this.academicRecordRepository = academicRecordRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<EnrollmentSummaryResponse> list(EnrollmentSearchCriteria criteria, Pageable pageable) {
        return PageResponse.from(enrollmentRepository.findAll(specification(criteria), pageable).map(this::toSummary));
    }

    @Transactional(readOnly = true)
    public EnrollmentResponse get(UUID id) {
        return toResponse(findEnrollment(id));
    }

    @Transactional
    public EnrollmentResponse create(EnrollmentRequest request) {
        Student student = studentRepository.findById(request.studentId())
                .orElseThrow(() -> new NotFoundException("Student not found"));
        SchoolYear schoolYear = schoolYearRepository.findById(request.schoolYearId())
                .orElseThrow(() -> new NotFoundException("School year not found"));
        Semester semester = semesterRepository.findById(request.semesterId())
                .orElseThrow(() -> new NotFoundException("Semester not found"));
        Section section = resolveSection(request.sectionId());
        validateSection(student, schoolYear, semester, section);
        validateNoDuplicateEnrollment(student.getId(), schoolYear.getId(), semester.getId());

        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setProgram(student.getProgram());
        enrollment.setSchoolYear(schoolYear);
        enrollment.setSemester(semester);
        enrollment.setSection(section);
        enrollment.setRemarks(request.remarks());
        enrollment.setStatus(EnrollmentStatus.DRAFT);
        Enrollment saved = enrollmentRepository.save(enrollment);
        recordStatusHistory(saved, null, EnrollmentStatus.DRAFT, "Enrollment created");
        return toResponse(saved);
    }

    @Transactional
    public EnrollmentResponse update(UUID id, EnrollmentUpdateRequest request) {
        Enrollment enrollment = findEnrollment(id);
        ensureDraft(enrollment);
        Section section = resolveSection(request.sectionId());
        validateSection(enrollment.getStudent(), enrollment.getSchoolYear(), enrollment.getSemester(), section);
        enrollment.setSection(section);
        enrollment.setRemarks(request.remarks());
        return toResponse(enrollment);
    }

    @Transactional
    public EnrollmentResponse addSubject(UUID enrollmentId, EnrollmentSubjectRequest request) {
        Enrollment enrollment = findEnrollment(enrollmentId);
        ensureDraft(enrollment);
        ClassSchedule schedule = classScheduleRepository.findById(request.scheduleId())
                .orElseThrow(() -> new NotFoundException("Schedule not found"));
        validateScheduleForEnrollment(enrollment, schedule);
        if (enrollmentSubjectRepository.existsByEnrollmentIdAndClassScheduleIdAndStatus(enrollmentId, schedule.getId(), EnrollmentSubjectStatus.ENROLLED)) {
            throw new BusinessRuleException("Schedule is already selected for this enrollment");
        }
        validateNoSelectedScheduleConflict(enrollment, schedule, null);

        EnrollmentSubject subject = new EnrollmentSubject();
        subject.setClassSchedule(schedule);
        subject.setStatus(EnrollmentSubjectStatus.ENROLLED);
        enrollment.addSubject(subject);
        enrollmentSubjectRepository.save(subject);
        return toResponse(enrollment);
    }

    @Transactional
    public EnrollmentResponse dropSubject(UUID enrollmentId, UUID subjectId) {
        Enrollment enrollment = findEnrollment(enrollmentId);
        ensureDraft(enrollment);
        EnrollmentSubject subject = enrollmentSubjectRepository.findByIdAndEnrollmentId(subjectId, enrollmentId)
                .orElseThrow(() -> new NotFoundException("Enrollment subject not found"));
        subject.setStatus(EnrollmentSubjectStatus.DROPPED);
        subject.setDroppedAt(Instant.now());
        return toResponse(enrollment);
    }

    @Transactional(readOnly = true)
    public EnrollmentValidationResponse validate(UUID id) {
        return validateEnrollment(findEnrollment(id));
    }

    @Transactional
    public EnrollmentResponse confirm(UUID id) {
        Enrollment enrollment = findEnrollment(id);
        ensureDraft(enrollment);
        EnrollmentValidationResponse validation = validateEnrollment(enrollment);
        if (!validation.valid()) {
            throw new BusinessRuleException("Enrollment has validation issues");
        }
        EnrollmentStatus previous = enrollment.getStatus();
        enrollment.setStatus(EnrollmentStatus.CONFIRMED);
        recordStatusHistory(enrollment, previous, EnrollmentStatus.CONFIRMED, "Enrollment confirmed");
        return toResponse(enrollment);
    }

    @Transactional
    public EnrollmentResponse cancel(UUID id) {
        Enrollment enrollment = findEnrollment(id);
        if (enrollment.getStatus() == EnrollmentStatus.CANCELLED) {
            throw new BusinessRuleException("Enrollment is already cancelled");
        }
        EnrollmentStatus previous = enrollment.getStatus();
        enrollment.setStatus(EnrollmentStatus.CANCELLED);
        recordStatusHistory(enrollment, previous, EnrollmentStatus.CANCELLED, "Enrollment cancelled");
        return toResponse(enrollment);
    }

    private void validateScheduleForEnrollment(Enrollment enrollment, ClassSchedule schedule) {
        if (schedule.getStatus() != ScheduleStatus.ACTIVE) {
            throw new BusinessRuleException("Only active schedules can be selected");
        }
        if (!schedule.getSchoolYear().getId().equals(enrollment.getSchoolYear().getId())
                || !schedule.getSemester().getId().equals(enrollment.getSemester().getId())) {
            throw new BusinessRuleException("Schedule term must match enrollment term");
        }
        if (enrollment.getSection() != null && !schedule.getSection().getId().equals(enrollment.getSection().getId())) {
            throw new BusinessRuleException("Schedule section must match enrollment section");
        }
        if (!schedule.getSection().getProgram().getId().equals(enrollment.getProgram().getId())) {
            throw new BusinessRuleException("Schedule program must match student program");
        }
        if (!courseExistsInStudentCurriculum(enrollment, schedule)) {
            throw new BusinessRuleException("Schedule course is not part of the student's curriculum");
        }
    }

    private EnrollmentValidationResponse validateEnrollment(Enrollment enrollment) {
        List<EnrollmentSubject> subjects = activeSubjects(enrollment);
        List<EnrollmentValidationIssueResponse> blocking = new ArrayList<>();
        List<EnrollmentValidationIssueResponse> warnings = new ArrayList<>();
        Map<UUID, CurriculumCourse> curriculumCoursesByCourseId = curriculumCoursesByCourseId(enrollment);
        Set<UUID> completedCourseIds = academicRecordRepository.findCompletedCourseIds(
                enrollment.getStudent().getId(),
                GradeRemark.PASSED,
                COMPLETED_GRADE_STATUSES
        );
        Set<UUID> selectedCourseIds = subjects.stream()
                .map(subject -> subject.getClassSchedule().getCourse().getId())
                .collect(Collectors.toSet());

        if (subjects.isEmpty()) {
            blocking.add(issue("NO_SUBJECTS", "Enrollment must have at least one selected subject", null, null));
        }
        for (EnrollmentSubject subject : subjects) {
            ClassSchedule schedule = subject.getClassSchedule();
            CurriculumCourse curriculumCourse = curriculumCoursesByCourseId.get(schedule.getCourse().getId());
            if (schedule.getStatus() != ScheduleStatus.ACTIVE) {
                blocking.add(issue("INACTIVE_SCHEDULE", "Selected schedule is not active", subject.getId(), schedule.getId()));
            }
            if (!schedule.getSchoolYear().getId().equals(enrollment.getSchoolYear().getId())
                    || !schedule.getSemester().getId().equals(enrollment.getSemester().getId())) {
                blocking.add(issue("TERM_MISMATCH", "Selected schedule term does not match enrollment term", subject.getId(), schedule.getId()));
            }
            if (enrollment.getSection() != null && !schedule.getSection().getId().equals(enrollment.getSection().getId())) {
                blocking.add(issue("SECTION_MISMATCH", "Selected schedule section does not match enrollment section", subject.getId(), schedule.getId()));
            }
            if (curriculumCourse == null) {
                blocking.add(issue("NON_CURRICULUM_COURSE", "Selected schedule course is not part of the student's curriculum", subject.getId(), schedule.getId()));
            } else {
                validatePrerequisitesAndCorequisites(curriculumCourse, completedCourseIds, selectedCourseIds, subject, blocking);
            }
        }
        for (int i = 0; i < subjects.size(); i++) {
            for (int j = i + 1; j < subjects.size(); j++) {
                if (hasMeetingConflict(subjects.get(i).getClassSchedule(), subjects.get(j).getClassSchedule())) {
                    blocking.add(issue("SCHEDULE_CONFLICT", "Selected schedules have overlapping meeting times", subjects.get(j).getId(), subjects.get(j).getClassSchedule().getId()));
                }
            }
        }

        BigDecimal totalCreditUnits = totalCreditUnits(subjects);
        return new EnrollmentValidationResponse(blocking.isEmpty(), blocking, warnings, totalCreditUnits, subjects.size());
    }

    private void validateNoSelectedScheduleConflict(Enrollment enrollment, ClassSchedule requested, UUID ignoredSubjectId) {
        for (EnrollmentSubject subject : activeSubjects(enrollment)) {
            if (ignoredSubjectId != null && ignoredSubjectId.equals(subject.getId())) {
                continue;
            }
            if (hasMeetingConflict(subject.getClassSchedule(), requested)) {
                throw new BusinessRuleException("Selected schedule conflicts with another enrolled subject");
            }
        }
    }

    private boolean hasMeetingConflict(ClassSchedule first, ClassSchedule second) {
        for (ScheduleMeeting firstMeeting : first.getMeetings()) {
            for (ScheduleMeeting secondMeeting : second.getMeetings()) {
                if (firstMeeting.getDayOfWeek() == secondMeeting.getDayOfWeek()
                        && firstMeeting.getStartTime().isBefore(secondMeeting.getEndTime())
                        && firstMeeting.getEndTime().isAfter(secondMeeting.getStartTime())) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean courseExistsInStudentCurriculum(Enrollment enrollment, ClassSchedule schedule) {
        return curriculumCoursesByCourseId(enrollment).containsKey(schedule.getCourse().getId());
    }

    private Map<UUID, CurriculumCourse> curriculumCoursesByCourseId(Enrollment enrollment) {
        return curriculumCourseRepository.findByCurriculumIdOrderByYearLevelAscSemesterAscSortOrderAsc(enrollment.getStudent().getCurriculum().getId())
                .stream()
                .collect(Collectors.toMap(
                        curriculumCourse -> curriculumCourse.getCourse().getId(),
                        Function.identity(),
                        (first, second) -> first
                ));
    }

    private void validatePrerequisitesAndCorequisites(
            CurriculumCourse curriculumCourse,
            Set<UUID> completedCourseIds,
            Set<UUID> selectedCourseIds,
            EnrollmentSubject subject,
            List<EnrollmentValidationIssueResponse> blocking
    ) {
        Course selectedCourse = curriculumCourse.getCourse();
        for (Course prerequisite : curriculumCourse.getPrerequisites()) {
            if (!completedCourseIds.contains(prerequisite.getId())) {
                blocking.add(issue(
                        "UNMET_PREREQUISITE",
                        selectedCourse.getCourseCode() + " requires prerequisite " + prerequisite.getCourseCode(),
                        subject.getId(),
                        subject.getClassSchedule().getId()
                ));
            }
        }
        for (Course corequisite : curriculumCourse.getCorequisites()) {
            if (!completedCourseIds.contains(corequisite.getId()) && !selectedCourseIds.contains(corequisite.getId())) {
                blocking.add(issue(
                        "MISSING_COREQUISITE",
                        selectedCourse.getCourseCode() + " requires corequisite " + corequisite.getCourseCode(),
                        subject.getId(),
                        subject.getClassSchedule().getId()
                ));
            }
        }
    }

    private void validateSection(Student student, SchoolYear schoolYear, Semester semester, Section section) {
        if (section == null) {
            return;
        }
        if (!section.getProgram().getId().equals(student.getProgram().getId())) {
            throw new BusinessRuleException("Section program must match student program");
        }
        if (!section.getSchoolYear().getId().equals(schoolYear.getId()) || !section.getSemester().getId().equals(semester.getId())) {
            throw new BusinessRuleException("Section term must match enrollment term");
        }
    }

    private void validateNoDuplicateEnrollment(UUID studentId, UUID schoolYearId, UUID semesterId) {
        if (enrollmentRepository.existsByStudentIdAndSchoolYearIdAndSemesterIdAndStatusIn(studentId, schoolYearId, semesterId, ACTIVE_ENROLLMENT_STATUSES)) {
            throw new BusinessRuleException("Student already has an active enrollment for this term");
        }
    }

    private void ensureDraft(Enrollment enrollment) {
        if (enrollment.getStatus() != EnrollmentStatus.DRAFT) {
            throw new BusinessRuleException("Only draft enrollments can be modified");
        }
    }

    private Section resolveSection(UUID sectionId) {
        if (sectionId == null) {
            return null;
        }
        return sectionRepository.findById(sectionId)
                .orElseThrow(() -> new NotFoundException("Section not found"));
    }

    private Enrollment findEnrollment(UUID id) {
        return enrollmentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Enrollment not found"));
    }

    private void recordStatusHistory(Enrollment enrollment, EnrollmentStatus fromStatus, EnrollmentStatus toStatus, String remarks) {
        EnrollmentStatusHistory history = new EnrollmentStatusHistory();
        history.setEnrollment(enrollment);
        history.setFromStatus(fromStatus);
        history.setToStatus(toStatus);
        history.setRemarks(remarks);
        statusHistoryRepository.save(history);
    }

    private EnrollmentResponse toResponse(Enrollment enrollment) {
        EnrollmentValidationResponse validation = validateEnrollment(enrollment);
        List<EnrollmentSubjectResponse> subjects = enrollment.getSubjects().stream()
                .sorted(Comparator.comparing(subject -> subject.getClassSchedule().getCourse().getCourseCode()))
                .map(this::toSubjectResponse)
                .toList();
        return new EnrollmentResponse(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getStudentNumber(),
                studentName(enrollment.getStudent()),
                enrollment.getProgram().getId(),
                enrollment.getProgram().getProgramCode(),
                enrollment.getSection() == null ? null : enrollment.getSection().getId(),
                enrollment.getSection() == null ? null : enrollment.getSection().getSectionCode(),
                enrollment.getSchoolYear().getId(),
                enrollment.getSchoolYear().getSchoolYear(),
                enrollment.getSemester().getId(),
                enrollment.getSemester().getName(),
                enrollment.getStatus(),
                enrollment.getRemarks(),
                validation.totalCreditUnits(),
                validation.subjectCount(),
                subjects,
                validation
        );
    }

    private EnrollmentSummaryResponse toSummary(Enrollment enrollment) {
        List<EnrollmentSubject> subjects = activeSubjects(enrollment);
        return new EnrollmentSummaryResponse(
                enrollment.getId(),
                enrollment.getStudent().getId(),
                enrollment.getStudent().getStudentNumber(),
                studentName(enrollment.getStudent()),
                enrollment.getProgram().getId(),
                enrollment.getProgram().getProgramCode(),
                enrollment.getSection() == null ? null : enrollment.getSection().getId(),
                enrollment.getSection() == null ? null : enrollment.getSection().getSectionCode(),
                enrollment.getSchoolYear().getId(),
                enrollment.getSchoolYear().getSchoolYear(),
                enrollment.getSemester().getId(),
                enrollment.getSemester().getName(),
                enrollment.getStatus(),
                totalCreditUnits(subjects),
                subjects.size()
        );
    }

    private EnrollmentSubjectResponse toSubjectResponse(EnrollmentSubject subject) {
        ClassSchedule schedule = subject.getClassSchedule();
        return new EnrollmentSubjectResponse(
                subject.getId(),
                schedule.getId(),
                schedule.getCourse().getId(),
                schedule.getCourse().getCourseCode(),
                schedule.getCourse().getCourseTitle(),
                schedule.getCourse().getCreditUnits(),
                schedule.getSection().getId(),
                schedule.getSection().getSectionCode(),
                schedule.getFaculty().getId(),
                facultyName(schedule.getFaculty()),
                schedule.getRoom().getId(),
                schedule.getRoom().getRoomCode(),
                subject.getStatus(),
                schedule.getMeetings().stream()
                        .sorted(Comparator.comparing(ScheduleMeeting::getDayOfWeek).thenComparing(ScheduleMeeting::getStartTime))
                        .map(meeting -> new ScheduleMeetingResponse(meeting.getId(), meeting.getDayOfWeek(), meeting.getStartTime(), meeting.getEndTime()))
                        .toList()
        );
    }

    private List<EnrollmentSubject> activeSubjects(Enrollment enrollment) {
        return enrollment.getSubjects().stream()
                .filter(subject -> subject.getStatus() == EnrollmentSubjectStatus.ENROLLED)
                .toList();
    }

    private BigDecimal totalCreditUnits(List<EnrollmentSubject> subjects) {
        return subjects.stream()
                .map(subject -> subject.getClassSchedule().getCourse().getCreditUnits())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private EnrollmentValidationIssueResponse issue(String code, String message, UUID subjectId, UUID scheduleId) {
        return new EnrollmentValidationIssueResponse(code, message, subjectId, scheduleId);
    }

    private Specification<Enrollment> specification(EnrollmentSearchCriteria criteria) {
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
                        cb.like(cb.lower(root.get("program").get("programCode")), term)
                ));
            }
            if (criteria.studentId() != null) predicate = cb.and(predicate, cb.equal(root.get("student").get("id"), criteria.studentId()));
            if (criteria.programId() != null) predicate = cb.and(predicate, cb.equal(root.get("program").get("id"), criteria.programId()));
            if (criteria.sectionId() != null) predicate = cb.and(predicate, cb.equal(root.get("section").get("id"), criteria.sectionId()));
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
        return String.join(" ", List.of(faculty.getFirstName(), blankToEmpty(faculty.getMiddleName()), faculty.getLastName(), blankToEmpty(faculty.getSuffix())))
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value;
    }
}
