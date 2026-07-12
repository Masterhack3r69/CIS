package com.school.sis.report;

import com.school.sis.audit.repository.AuditLogRepository;
import com.school.sis.auth.security.SisUserDetails;
import com.school.sis.common.exception.NotFoundException;
import com.school.sis.curriculum.entity.Curriculum;
import com.school.sis.curriculum.entity.CurriculumCourse;
import com.school.sis.curriculum.entity.CurriculumStatus;
import com.school.sis.curriculum.entity.RequiredStatus;
import com.school.sis.curriculum.repository.CurriculumCourseRepository;
import com.school.sis.curriculum.repository.CurriculumRepository;
import com.school.sis.enrollment.dto.EnrollmentRequest;
import com.school.sis.enrollment.dto.EnrollmentResponse;
import com.school.sis.enrollment.dto.EnrollmentSubjectRequest;
import com.school.sis.enrollment.service.EnrollmentService;
import com.school.sis.fee.dto.AssessmentResponse;
import com.school.sis.fee.dto.FeeItemRequest;
import com.school.sis.fee.dto.FeeRuleRequest;
import com.school.sis.fee.entity.FeeCategory;
import com.school.sis.fee.entity.FeeComputationType;
import com.school.sis.fee.service.AssessmentService;
import com.school.sis.fee.service.FeeService;
import com.school.sis.grade.dto.GradeClassResponse;
import com.school.sis.grade.dto.GradeEncodeRequest;
import com.school.sis.grade.dto.GradeEntryRequest;
import com.school.sis.grade.service.GradeService;
import com.school.sis.report.repository.GeneratedReportRepository;
import com.school.sis.report.service.PdfReport;
import com.school.sis.report.service.ReportService;
import com.school.sis.schedule.dto.ScheduleMeetingRequest;
import com.school.sis.schedule.dto.ScheduleRequest;
import com.school.sis.schedule.dto.ScheduleResponse;
import com.school.sis.schedule.entity.ScheduleStatus;
import com.school.sis.schedule.service.ScheduleService;
import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.entity.Course;
import com.school.sis.setup.entity.CourseType;
import com.school.sis.setup.entity.DegreeType;
import com.school.sis.setup.entity.Department;
import com.school.sis.setup.entity.EmploymentStatus;
import com.school.sis.setup.entity.Faculty;
import com.school.sis.setup.entity.FacultyType;
import com.school.sis.setup.entity.Program;
import com.school.sis.setup.entity.Room;
import com.school.sis.setup.entity.SchoolYear;
import com.school.sis.setup.entity.Section;
import com.school.sis.setup.entity.Semester;
import com.school.sis.setup.repository.CourseRepository;
import com.school.sis.setup.repository.DepartmentRepository;
import com.school.sis.setup.repository.FacultyRepository;
import com.school.sis.setup.repository.ProgramRepository;
import com.school.sis.setup.repository.RoomRepository;
import com.school.sis.setup.repository.SchoolYearRepository;
import com.school.sis.setup.repository.SectionRepository;
import com.school.sis.setup.repository.SemesterRepository;
import com.school.sis.student.entity.AcademicStatus;
import com.school.sis.student.entity.Gender;
import com.school.sis.student.entity.Student;
import com.school.sis.student.entity.StudentClassification;
import com.school.sis.student.entity.StudentStatus;
import com.school.sis.student.repository.StudentRepository;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.text.PDFTextStripper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ReportServiceTests {

    private final ReportService reportService;
    private final GeneratedReportRepository generatedReportRepository;
    private final AuditLogRepository auditLogRepository;
    private final FeeService feeService;
    private final AssessmentService assessmentService;
    private final GradeService gradeService;
    private final EnrollmentService enrollmentService;
    private final ScheduleService scheduleService;
    private final DepartmentRepository departmentRepository;
    private final ProgramRepository programRepository;
    private final CourseRepository courseRepository;
    private final FacultyRepository facultyRepository;
    private final RoomRepository roomRepository;
    private final SchoolYearRepository schoolYearRepository;
    private final SemesterRepository semesterRepository;
    private final SectionRepository sectionRepository;
    private final CurriculumRepository curriculumRepository;
    private final CurriculumCourseRepository curriculumCourseRepository;
    private final StudentRepository studentRepository;

    private Program program;
    private Course course;
    private Faculty faculty;
    private Room room;
    private SchoolYear schoolYear;
    private Semester semester;
    private Section section;
    private Student student;
    private Curriculum curriculum;
    private ScheduleResponse schedule;
    private EnrollmentResponse enrollment;
    private AssessmentResponse assessment;

    @Autowired
    ReportServiceTests(
            ReportService reportService,
            GeneratedReportRepository generatedReportRepository,
            AuditLogRepository auditLogRepository,
            FeeService feeService,
            AssessmentService assessmentService,
            GradeService gradeService,
            EnrollmentService enrollmentService,
            ScheduleService scheduleService,
            DepartmentRepository departmentRepository,
            ProgramRepository programRepository,
            CourseRepository courseRepository,
            FacultyRepository facultyRepository,
            RoomRepository roomRepository,
            SchoolYearRepository schoolYearRepository,
            SemesterRepository semesterRepository,
            SectionRepository sectionRepository,
            CurriculumRepository curriculumRepository,
            CurriculumCourseRepository curriculumCourseRepository,
            StudentRepository studentRepository
    ) {
        this.reportService = reportService;
        this.generatedReportRepository = generatedReportRepository;
        this.auditLogRepository = auditLogRepository;
        this.feeService = feeService;
        this.assessmentService = assessmentService;
        this.gradeService = gradeService;
        this.enrollmentService = enrollmentService;
        this.scheduleService = scheduleService;
        this.departmentRepository = departmentRepository;
        this.programRepository = programRepository;
        this.courseRepository = courseRepository;
        this.facultyRepository = facultyRepository;
        this.roomRepository = roomRepository;
        this.schoolYearRepository = schoolYearRepository;
        this.semesterRepository = semesterRepository;
        this.sectionRepository = sectionRepository;
        this.curriculumRepository = curriculumRepository;
        this.curriculumCourseRepository = curriculumCourseRepository;
        this.studentRepository = studentRepository;
    }

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        Department department = new Department();
        department.setDepartmentCode("REP-CCS-" + suffix);
        department.setDepartmentName("Report Department " + suffix);
        department.setStatus(ActiveStatus.ACTIVE);
        department = departmentRepository.save(department);

        program = new Program();
        program.setProgramCode("REP-BSIT-" + suffix);
        program.setProgramName("Report Program");
        program.setDepartment(department);
        program.setDegreeType(DegreeType.BACHELOR);
        program.setProgramDuration(4);
        program.setStatus(ActiveStatus.ACTIVE);
        program = programRepository.save(program);

        course = new Course();
        course.setCourseCode("REP-101-" + suffix);
        course.setCourseTitle("Report Writing");
        course.setCourseType(CourseType.MAJOR);
        course.setDepartment(department);
        course.setLectureHoursPerWeek(BigDecimal.valueOf(3));
        course.setLaboratoryHoursPerWeek(BigDecimal.ZERO);
        course.setCreditUnits(BigDecimal.valueOf(3));
        course.setStatus(ActiveStatus.ACTIVE);
        course = courseRepository.save(course);

        faculty = new Faculty();
        faculty.setEmployeeNumber("REP-FAC-" + suffix);
        faculty.setFirstName("Report");
        faculty.setLastName("Faculty");
        faculty.setEmail("report-faculty-" + suffix + "@sis.local");
        faculty.setDepartment(department);
        faculty.setEmploymentStatus(EmploymentStatus.FULL_TIME);
        faculty.setFacultyType(FacultyType.INSTRUCTOR);
        faculty.setStatus(ActiveStatus.ACTIVE);
        faculty = facultyRepository.save(faculty);

        room = new Room();
        room.setRoomCode("REP-RM-" + suffix);
        room.setRoomName("Report Room");
        room.setCapacity(40);
        room.setStatus(ActiveStatus.ACTIVE);
        room = roomRepository.save(room);

        schoolYear = new SchoolYear();
        schoolYear.setSchoolYear("REP-2026-" + suffix);
        schoolYear.setActive(true);
        schoolYear = schoolYearRepository.save(schoolYear);

        semester = new Semester();
        semester.setName("REPFIRST" + suffix);
        semester.setSortOrder(1);
        semester.setActive(true);
        semester = semesterRepository.save(semester);

        curriculum = new Curriculum();
        curriculum.setProgram(program);
        curriculum.setCurriculumCode("REP-CUR-" + suffix);
        curriculum.setCurriculumName("Report Curriculum");
        curriculum.setEffectiveSchoolYear("2026-2027");
        curriculum.setVersion("1");
        curriculum.setStatus(CurriculumStatus.ACTIVE);
        curriculum = curriculumRepository.save(curriculum);

        section = new Section();
        section.setSectionCode("REP-1A-" + suffix);
        section.setProgram(program);
        section.setCurriculum(curriculum);
        section.setSchoolYear(schoolYear);
        section.setSemester(semester);
        section.setYearLevel(1);
        section.setStatus(ActiveStatus.ACTIVE);
        section = sectionRepository.save(section);

        CurriculumCourse curriculumCourse = new CurriculumCourse();
        curriculumCourse.setCurriculum(curriculum);
        curriculumCourse.setYearLevel(1);
        curriculumCourse.setSemester(semester.getName());
        curriculumCourse.setCourse(course);
        curriculumCourse.setSortOrder(1);
        curriculumCourse.setRequiredStatus(RequiredStatus.REQUIRED);
        curriculumCourseRepository.save(curriculumCourse);

        student = new Student();
        student.setStudentNumber("REP-S-" + suffix);
        student.setFirstName("Report");
        student.setLastName("Student");
        student.setGender(Gender.OTHER);
        student.setBirthdate(LocalDate.of(2005, 1, 1));
        student.setStatus(StudentStatus.ACTIVE);
        student.setProgram(program);
        student.setCurriculum(curriculum);
        student.setYearLevel(1);
        student.setDateAdmitted(LocalDate.of(2026, 6, 1));
        student.setSchoolYearAdmitted("2026-2027");
        student.setClassification(StudentClassification.REGULAR);
        student.setAcademicStatus(AcademicStatus.REGULAR);
        student = studentRepository.save(student);

        schedule = scheduleService.create(new ScheduleRequest(
                section.getId(),
                course.getId(),
                faculty.getId(),
                room.getId(),
                40,
                ScheduleStatus.ACTIVE,
                List.of(new ScheduleMeetingRequest(DayOfWeek.MONDAY, LocalTime.parse("09:00"), LocalTime.parse("10:00")))
        ));
        enrollment = enrollmentService.create(new EnrollmentRequest(student.getId(), schoolYear.getId(), semester.getId(), student.getYearLevel(), section.getId(), null));
        enrollmentService.addSubject(enrollment.id(), new EnrollmentSubjectRequest(schedule.id()));
        enrollment = enrollmentService.confirm(enrollment.id());

        feeService.create(new FeeItemRequest(
                "REP-FEE-" + suffix,
                "Report Fee",
                FeeCategory.MISCELLANEOUS,
                null,
                ActiveStatus.ACTIVE,
                List.of(new FeeRuleRequest(null, schoolYear.getId(), semester.getId(), program.getId(), 1, FeeComputationType.FIXED_AMOUNT, BigDecimal.valueOf(1200), ActiveStatus.ACTIVE))
        ));
        assessment = assessmentService.generate(enrollment.id());

        GradeClassResponse grades = gradeService.classGrades(schedule.id(), facultyUser());
        gradeService.encode(schedule.id(), new GradeEncodeRequest(List.of(
                new GradeEntryRequest(grades.grades().getFirst().enrollmentSubjectId(), new BigDecimal("1.25"), null)
        )), facultyUser());
        gradeService.submit(schedule.id(), facultyUser());
        gradeService.approve(schedule.id(), approverUser());
        gradeService.lock(schedule.id(), approverUser());
    }

    @Test
    void coreReportsReturnPdfBytesWithExpectedContent() throws IOException {
        assertPdf(reportService.studentProfile(student.getId(), null), "Student Profile Report", "Report Student");
        assertPdf(reportService.curriculumChecklist(student.getId(), null), "Curriculum Checklist", course.getCourseCode());
        assertPdf(reportService.enrollmentForm(enrollment.id(), null), "Enrollment Form", course.getCourseCode());
        assertPdf(reportService.assessmentForm(assessment.id(), null), "Assessment Form", "1200");
        assertPdf(reportService.classList(schedule.id(), null), "Class List", student.getStudentNumber());
        assertPdf(reportService.gradeSheet(schedule.id(), null), "Grade Sheet", "1.25");
        assertPdf(reportService.gradeSlip(student.getId(), null), "Grade Slip", "PASSED");
    }

    @Test
    void successfulGenerationIsLogged() {
        long before = generatedReportRepository.countByReportTypeAndTargetEntityId("STUDENT_PROFILE", student.getId());
        long auditBefore = auditLogRepository.findAll().stream()
                .filter(log -> log.getAction().equals("REPORT_GENERATED"))
                .filter(log -> log.getEntityId().equals(student.getId()))
                .filter(log -> log.getNewValue().get("reportType").asText().equals("STUDENT_PROFILE"))
                .count();

        reportService.studentProfile(student.getId(), null);

        assertThat(generatedReportRepository.countByReportTypeAndTargetEntityId("STUDENT_PROFILE", student.getId()))
                .isEqualTo(before + 1);
        assertThat(auditLogRepository.findAll().stream()
                .filter(log -> log.getAction().equals("REPORT_GENERATED"))
                .filter(log -> log.getEntityId().equals(student.getId()))
                .filter(log -> log.getNewValue().get("reportType").asText().equals("STUDENT_PROFILE"))
                .count()).isEqualTo(auditBefore + 1);
    }

    @Test
    void missingTargetThrowsNotFound() {
        assertThatThrownBy(() -> reportService.studentProfile(UUID.randomUUID(), null))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Student not found");
    }

    private void assertPdf(PdfReport report, String... expectedText) throws IOException {
        assertThat(report.bytes()).startsWith("%PDF".getBytes());
        assertThat(report.bytes().length).isGreaterThan(500);
        try (var document = Loader.loadPDF(report.bytes())) {
            String text = new PDFTextStripper().getText(document);
            for (String expected : expectedText) {
                assertThat(text).contains(expected);
            }
        }
    }

    private SisUserDetails facultyUser() {
        SisUserDetails userDetails = mock(SisUserDetails.class);
        when(userDetails.id()).thenReturn(UUID.randomUUID());
        when(userDetails.facultyId()).thenReturn(faculty.getId());
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("GRADE_ENCODE"));
        doReturn(authorities).when(userDetails).getAuthorities();
        return userDetails;
    }

    private SisUserDetails approverUser() {
        SisUserDetails userDetails = mock(SisUserDetails.class);
        when(userDetails.id()).thenReturn(UUID.randomUUID());
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("GRADE_APPROVE"));
        doReturn(authorities).when(userDetails).getAuthorities();
        return userDetails;
    }
}
