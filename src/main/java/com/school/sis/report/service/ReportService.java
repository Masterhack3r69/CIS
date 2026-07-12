package com.school.sis.report.service;

import com.school.sis.auth.entity.User;
import com.school.sis.auth.repository.UserRepository;
import com.school.sis.auth.security.SisUserDetails;
import com.school.sis.audit.service.AuditService;
import com.school.sis.common.exception.NotFoundException;
import com.school.sis.curriculum.entity.CurriculumCourse;
import com.school.sis.curriculum.repository.CurriculumCourseRepository;
import com.school.sis.enrollment.entity.Enrollment;
import com.school.sis.enrollment.entity.EnrollmentSubject;
import com.school.sis.enrollment.entity.EnrollmentSubjectStatus;
import com.school.sis.enrollment.repository.EnrollmentRepository;
import com.school.sis.enrollment.repository.EnrollmentSubjectRepository;
import com.school.sis.fee.entity.Assessment;
import com.school.sis.fee.entity.AssessmentItem;
import com.school.sis.fee.repository.AssessmentRepository;
import com.school.sis.grade.entity.AcademicRecord;
import com.school.sis.grade.entity.Grade;
import com.school.sis.grade.repository.AcademicRecordRepository;
import com.school.sis.grade.repository.GradeRepository;
import com.school.sis.report.config.SchoolProperties;
import com.school.sis.report.entity.GeneratedReport;
import com.school.sis.report.repository.GeneratedReportRepository;
import com.school.sis.schedule.entity.ClassSchedule;
import com.school.sis.schedule.entity.ScheduleMeeting;
import com.school.sis.schedule.repository.ClassScheduleRepository;
import com.school.sis.student.entity.Student;
import com.school.sis.student.entity.StudentDocument;
import com.school.sis.student.repository.StudentDocumentRepository;
import com.school.sis.student.repository.StudentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ReportService {

    private final SchoolProperties schoolProperties;
    private final StudentRepository studentRepository;
    private final StudentDocumentRepository studentDocumentRepository;
    private final CurriculumCourseRepository curriculumCourseRepository;
    private final AcademicRecordRepository academicRecordRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollmentSubjectRepository enrollmentSubjectRepository;
    private final AssessmentRepository assessmentRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final GradeRepository gradeRepository;
    private final GeneratedReportRepository generatedReportRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    public ReportService(
            SchoolProperties schoolProperties,
            StudentRepository studentRepository,
            StudentDocumentRepository studentDocumentRepository,
            CurriculumCourseRepository curriculumCourseRepository,
            AcademicRecordRepository academicRecordRepository,
            EnrollmentRepository enrollmentRepository,
            EnrollmentSubjectRepository enrollmentSubjectRepository,
            AssessmentRepository assessmentRepository,
            ClassScheduleRepository classScheduleRepository,
            GradeRepository gradeRepository,
            GeneratedReportRepository generatedReportRepository,
            UserRepository userRepository,
            AuditService auditService
    ) {
        this.schoolProperties = schoolProperties;
        this.studentRepository = studentRepository;
        this.studentDocumentRepository = studentDocumentRepository;
        this.curriculumCourseRepository = curriculumCourseRepository;
        this.academicRecordRepository = academicRecordRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.enrollmentSubjectRepository = enrollmentSubjectRepository;
        this.assessmentRepository = assessmentRepository;
        this.classScheduleRepository = classScheduleRepository;
        this.gradeRepository = gradeRepository;
        this.generatedReportRepository = generatedReportRepository;
        this.userRepository = userRepository;
        this.auditService = auditService;
    }

    @Transactional
    public PdfReport studentProfile(UUID studentId, SisUserDetails userDetails) {
        Student student = findStudent(studentId);
        List<StudentDocument> documents = studentDocumentRepository.findByStudentIdOrderByCreatedAtDesc(studentId);
        try (PdfReportBuilder pdf = builder(userDetails)) {
            pdf.start("Student Profile Report");
            studentHeader(pdf, student);
            pdf.section("Personal Information");
            pdf.field("Gender", student.getGender());
            pdf.field("Birthdate", student.getBirthdate());
            pdf.field("Birthplace", student.getBirthplace());
            pdf.field("Civil Status", student.getCivilStatus());
            pdf.field("Nationality", student.getNationality());
            pdf.field("Religion", student.getReligion());
            pdf.field("Status", student.getStatus());
            pdf.section("Contact Information");
            if (student.getContact() == null) {
                pdf.paragraph("No contact information recorded.");
            } else {
                pdf.field("Email", student.getContact().getEmailAddress());
                pdf.field("Mobile", student.getContact().getMobileNumber());
                pdf.field("Current Address", student.getContact().getCurrentAddress());
                pdf.field("Permanent Address", student.getContact().getPermanentAddress());
                pdf.field("Emergency Contact", student.getContact().getEmergencyContactName());
                pdf.field("Emergency Number", student.getContact().getEmergencyContactNumber());
            }
            pdf.section("Family Background");
            if (student.getFamilyBackground() == null) {
                pdf.paragraph("No family background recorded.");
            } else {
                pdf.field("Father", student.getFamilyBackground().getFatherName());
                pdf.field("Mother", student.getFamilyBackground().getMotherName());
                pdf.field("Guardian", student.getFamilyBackground().getGuardianName());
                pdf.field("Guardian Contact", student.getFamilyBackground().getGuardianContactNumber());
            }
            pdf.section("Educational Background");
            if (student.getEducationalBackground() == null) {
                pdf.paragraph("No educational background recorded.");
            } else {
                pdf.field("Elementary", student.getEducationalBackground().getElementarySchoolName());
                pdf.field("Junior High", student.getEducationalBackground().getJuniorHighSchoolName());
                pdf.field("Senior High", student.getEducationalBackground().getSeniorHighSchoolName());
                pdf.field("Previous College", student.getEducationalBackground().getPreviousCollege());
                pdf.field("Admission Type", student.getEducationalBackground().getAdmissionType());
            }
            pdf.section("Academic Information");
            academicHeader(pdf, student);
            pdf.section("Document Summary");
            pdf.table(new String[]{"Type", "File", "Status"}, documents.stream()
                    .map(document -> new String[]{document.getDocumentType(), document.getFileName(), value(document.getVerificationStatus())})
                    .toList());
            log("STUDENT_PROFILE", "Student", studentId, userDetails);
            return new PdfReport("student-profile-" + student.getStudentNumber() + ".pdf", pdf.finish());
        }
    }

    @Transactional
    public PdfReport curriculumChecklist(UUID studentId, SisUserDetails userDetails) {
        Student student = findStudent(studentId);
        List<CurriculumCourse> courses = curriculumCourseRepository.findByCurriculumIdOrderByYearLevelAscSemesterAscSortOrderAsc(student.getCurriculum().getId());
        Map<UUID, AcademicRecord> recordsByCourse = academicRecordRepository.findByStudentIdOrderBySchoolYearSchoolYearAscSemesterSortOrderAscCourseCodeAsc(studentId)
                .stream()
                .collect(Collectors.toMap(record -> record.getCourse().getId(), Function.identity(), (first, second) -> second));
        try (PdfReportBuilder pdf = builder(userDetails)) {
            pdf.start("Curriculum Checklist");
            studentHeader(pdf, student);
            pdf.section("Curriculum");
            pdf.field("Curriculum", student.getCurriculum().getCurriculumCode());
            pdf.table(new String[]{"Year", "Semester", "Course", "Units", "Grade", "Remarks", "Earned"}, courses.stream()
                    .map(course -> {
                        AcademicRecord record = recordsByCourse.get(course.getCourse().getId());
                        return new String[]{
                                String.valueOf(course.getYearLevel()),
                                course.getSemester(),
                                course.getCourse().getCourseCode() + " - " + course.getCourse().getCourseTitle(),
                                money(course.getCourse().getCreditUnits()),
                                record == null ? "" : money(record.getFinalGrade()),
                                record == null ? "" : value(record.getRemarks()),
                                record == null ? "0" : money(record.getEarnedUnits())
                        };
                    })
                    .toList());
            log("CURRICULUM_CHECKLIST", "Student", studentId, userDetails);
            return new PdfReport("curriculum-checklist-" + student.getStudentNumber() + ".pdf", pdf.finish());
        }
    }

    @Transactional
    public PdfReport enrollmentForm(UUID enrollmentId, SisUserDetails userDetails) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new NotFoundException("Enrollment not found"));
        try (PdfReportBuilder pdf = builder(userDetails)) {
            pdf.start("Enrollment Form");
            studentHeader(pdf, enrollment.getStudent());
            pdf.section("Enrollment");
            pdf.field("School Year", enrollment.getSchoolYear().getSchoolYear());
            pdf.field("Semester", enrollment.getSemester().getName());
            pdf.field("Program", enrollment.getProgram().getProgramCode());
            pdf.field("Section", enrollment.getSection() == null ? "" : enrollment.getSection().getSectionCode());
            pdf.field("Status", enrollment.getStatus());
            pdf.section("Selected Subjects");
            pdf.table(new String[]{"Course", "Units", "Faculty", "Room", "Schedule"}, activeSubjects(enrollment).stream()
                    .map(subject -> new String[]{
                            subject.getClassSchedule().getCourse().getCourseCode() + " - " + subject.getClassSchedule().getCourse().getCourseTitle(),
                            money(subject.getClassSchedule().getCourse().getCreditUnits()),
                            facultyName(subject.getClassSchedule().getFaculty()),
                            subject.getClassSchedule().getRoom().getRoomCode(),
                            meetings(subject.getClassSchedule().getMeetings())
                    })
                    .toList());
            log("ENROLLMENT_FORM", "Enrollment", enrollmentId, userDetails);
            return new PdfReport("enrollment-form-" + enrollment.getStudent().getStudentNumber() + ".pdf", pdf.finish());
        }
    }

    @Transactional
    public PdfReport assessmentForm(UUID assessmentId, SisUserDetails userDetails) {
        Assessment assessment = assessmentRepository.findById(assessmentId)
                .orElseThrow(() -> new NotFoundException("Assessment not found"));
        try (PdfReportBuilder pdf = builder(userDetails)) {
            pdf.start("Assessment Form");
            studentHeader(pdf, assessment.getStudent());
            pdf.section("Assessment Summary");
            pdf.field("School Year", assessment.getSchoolYear().getSchoolYear());
            pdf.field("Semester", assessment.getSemester().getName());
            pdf.field("Total Units", money(assessment.getTotalUnits()));
            pdf.field("Total Assessment", money(assessment.getTotalAssessment()));
            pdf.field("Amount Paid", money(assessment.getAmountPaid()));
            pdf.field("Balance", money(assessment.getBalance()));
            pdf.field("Status", assessment.getStatus());
            pdf.section("Assessment Items");
            pdf.table(new String[]{"Description", "Category", "Qty", "Unit", "Total"}, assessment.getItems().stream()
                    .sorted(Comparator.comparing(AssessmentItem::getDescription))
                    .map(item -> new String[]{
                            item.getDescription(),
                            value(item.getCategory()),
                            money(item.getQuantity()),
                            money(item.getUnitAmount()),
                            money(item.getTotalAmount())
                    })
                    .toList());
            log("ASSESSMENT_FORM", "Assessment", assessmentId, userDetails);
            return new PdfReport("assessment-" + assessment.getStudent().getStudentNumber() + ".pdf", pdf.finish());
        }
    }

    @Transactional
    public PdfReport classList(UUID scheduleId, SisUserDetails userDetails) {
        ClassSchedule schedule = findSchedule(scheduleId);
        List<EnrollmentSubject> subjects = enrollmentSubjectRepository.findConfirmedEnrolledSubjectsByScheduleId(scheduleId);
        try (PdfReportBuilder pdf = builder(userDetails)) {
            classHeader(pdf, schedule, "Class List");
            pdf.section("Students");
            pdf.table(new String[]{"Student Number", "Name", "Program", "Section"}, subjects.stream()
                    .map(subject -> new String[]{
                            subject.getEnrollment().getStudent().getStudentNumber(),
                            studentName(subject.getEnrollment().getStudent()),
                            subject.getEnrollment().getProgram().getProgramCode(),
                            subject.getEnrollment().getSection() == null ? "" : subject.getEnrollment().getSection().getSectionCode()
                    })
                    .toList());
            log("CLASS_LIST", "ClassSchedule", scheduleId, userDetails);
            return new PdfReport("class-list-" + schedule.getCourse().getCourseCode() + ".pdf", pdf.finish());
        }
    }

    @Transactional
    public PdfReport gradeSheet(UUID scheduleId, SisUserDetails userDetails) {
        ClassSchedule schedule = findSchedule(scheduleId);
        List<EnrollmentSubject> subjects = enrollmentSubjectRepository.findConfirmedEnrolledSubjectsByScheduleId(scheduleId);
        Map<UUID, Grade> gradesBySubject = gradeRepository.findByEnrollmentSubjectIdIn(subjects.stream().map(EnrollmentSubject::getId).toList())
                .stream()
                .collect(Collectors.toMap(grade -> grade.getEnrollmentSubject().getId(), Function.identity()));
        try (PdfReportBuilder pdf = builder(userDetails)) {
            classHeader(pdf, schedule, "Grade Sheet");
            pdf.section("Grades");
            pdf.table(new String[]{"Student Number", "Name", "Grade", "Remarks", "Status"}, subjects.stream()
                    .map(subject -> {
                        Grade grade = gradesBySubject.get(subject.getId());
                        return new String[]{
                                subject.getEnrollment().getStudent().getStudentNumber(),
                                studentName(subject.getEnrollment().getStudent()),
                                grade == null ? "" : money(grade.getFinalGrade()),
                                grade == null ? "" : value(grade.getRemarks()),
                                grade == null ? "DRAFT" : value(grade.getStatus())
                        };
                    })
                    .toList());
            log("GRADE_SHEET", "ClassSchedule", scheduleId, userDetails);
            return new PdfReport("grade-sheet-" + schedule.getCourse().getCourseCode() + ".pdf", pdf.finish());
        }
    }

    @Transactional
    public PdfReport gradeSlip(UUID studentId, SisUserDetails userDetails) {
        Student student = findStudent(studentId);
        List<AcademicRecord> records = academicRecordRepository.findByStudentIdOrderBySchoolYearSchoolYearAscSemesterSortOrderAscCourseCodeAsc(studentId);
        try (PdfReportBuilder pdf = builder(userDetails)) {
            pdf.start("Grade Slip");
            studentHeader(pdf, student);
            pdf.section("Locked Grades");
            pdf.table(new String[]{"Term", "Course", "Units", "Grade", "Remarks", "Earned"}, records.stream()
                    .map(record -> new String[]{
                            record.getSchoolYear().getSchoolYear() + " " + record.getSemester().getName(),
                            record.getCourseCode() + " - " + record.getCourseTitle(),
                            money(record.getCreditUnits()),
                            money(record.getFinalGrade()),
                            value(record.getRemarks()),
                            money(record.getEarnedUnits())
                    })
                    .toList());
            log("GRADE_SLIP", "Student", studentId, userDetails);
            return new PdfReport("grade-slip-" + student.getStudentNumber() + ".pdf", pdf.finish());
        }
    }

    private PdfReportBuilder builder(SisUserDetails userDetails) {
        return new PdfReportBuilder(schoolProperties, userDetails == null ? null : userDetails.fullName());
    }

    private void studentHeader(PdfReportBuilder pdf, Student student) {
        pdf.field("Student Number", student.getStudentNumber());
        pdf.field("Student Name", studentName(student));
    }

    private void academicHeader(PdfReportBuilder pdf, Student student) {
        pdf.field("Program", student.getProgram().getProgramCode() + " - " + student.getProgram().getProgramName());
        pdf.field("Curriculum", student.getCurriculum().getCurriculumCode());
        pdf.field("Year Level", student.getYearLevel());
        Enrollment latestEnrollment = enrollmentRepository.findFirstByStudentIdOrderBySchoolYearSchoolYearDescSemesterSortOrderDesc(student.getId()).orElse(null);
        pdf.field("Semester", latestEnrollment != null ? latestEnrollment.getSemester().getName() : "");
        pdf.field("Section", (latestEnrollment != null && latestEnrollment.getSection() != null) ? latestEnrollment.getSection().getSectionCode() : "");
        pdf.field("Academic Status", student.getAcademicStatus());
    }

    private void classHeader(PdfReportBuilder pdf, ClassSchedule schedule, String title) {
        pdf.start(title);
        pdf.field("Course", schedule.getCourse().getCourseCode() + " - " + schedule.getCourse().getCourseTitle());
        pdf.field("Section", schedule.getSection().getSectionCode());
        pdf.field("Faculty", facultyName(schedule.getFaculty()));
        pdf.field("Room", schedule.getRoom().getRoomCode());
        pdf.field("Term", schedule.getSchoolYear().getSchoolYear() + " " + schedule.getSemester().getName());
        pdf.field("Schedule", meetings(schedule.getMeetings()));
    }

    private List<EnrollmentSubject> activeSubjects(Enrollment enrollment) {
        return enrollment.getSubjects().stream()
                .filter(subject -> subject.getStatus() == EnrollmentSubjectStatus.ENROLLED)
                .sorted(Comparator.comparing(subject -> subject.getClassSchedule().getCourse().getCourseCode()))
                .toList();
    }

    private Student findStudent(UUID studentId) {
        return studentRepository.findById(studentId)
                .orElseThrow(() -> new NotFoundException("Student not found"));
    }

    private ClassSchedule findSchedule(UUID scheduleId) {
        return classScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new NotFoundException("Schedule not found"));
    }

    private void log(String reportType, String targetType, UUID targetId, SisUserDetails userDetails) {
        GeneratedReport report = new GeneratedReport();
        report.setReportType(reportType);
        report.setTargetEntityType(targetType);
        report.setTargetEntityId(targetId);
        User generatedBy = null;
        if (userDetails != null) {
            generatedBy = userRepository.findById(userDetails.id()).orElse(null);
            report.setGeneratedBy(generatedBy);
        }
        GeneratedReport saved = generatedReportRepository.save(report);
        auditService.log(generatedBy, "REPORT_GENERATED", "REPORT", targetType, targetId, null,
                Map.of("reportType", reportType, "generatedReportId", saved.getId()));
    }

    private String meetings(List<ScheduleMeeting> meetings) {
        return meetings.stream()
                .sorted(Comparator.comparing(ScheduleMeeting::getDayOfWeek).thenComparing(ScheduleMeeting::getStartTime))
                .map(meeting -> meeting.getDayOfWeek() + " " + meeting.getStartTime() + "-" + meeting.getEndTime())
                .collect(Collectors.joining(", "));
    }

    private String studentName(Student student) {
        return String.join(" ", List.of(student.getFirstName(), blankToEmpty(student.getMiddleName()), student.getLastName(), blankToEmpty(student.getSuffix())))
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String facultyName(com.school.sis.setup.entity.Faculty faculty) {
        return String.join(" ", List.of(faculty.getFirstName(), blankToEmpty(faculty.getMiddleName()), faculty.getLastName(), blankToEmpty(faculty.getSuffix())))
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String money(BigDecimal value) {
        return value == null ? "" : value.stripTrailingZeros().toPlainString();
    }

    private String value(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value;
    }
}
