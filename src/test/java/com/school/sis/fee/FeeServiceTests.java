package com.school.sis.fee;

import com.school.sis.common.exception.BusinessRuleException;
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
import com.school.sis.fee.dto.AssessmentStatusRequest;
import com.school.sis.fee.dto.FeeRequest;
import com.school.sis.fee.dto.FeeResponse;
import com.school.sis.fee.dto.FeeRuleRequest;
import com.school.sis.fee.entity.AssessmentStatus;
import com.school.sis.fee.entity.FeeRuleType;
import com.school.sis.fee.service.FeeService;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class FeeServiceTests {

    private final FeeService feeService;
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
    private Course courseOne;
    private Course courseTwo;
    private Faculty facultyOne;
    private Faculty facultyTwo;
    private Room roomOne;
    private Room roomTwo;
    private SchoolYear schoolYear;
    private Semester semester;
    private Section section;
    private Student student;
    private String suffix;

    @Autowired
    FeeServiceTests(
            FeeService feeService,
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
        this.feeService = feeService;
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
        suffix = UUID.randomUUID().toString().substring(0, 8);

        Department department = new Department();
        department.setDepartmentCode("FIN-CCS-" + suffix);
        department.setDepartmentName("Finance Test College " + suffix);
        department.setStatus(ActiveStatus.ACTIVE);
        department = departmentRepository.save(department);

        program = new Program();
        program.setProgramCode("FIN-BSIT-" + suffix);
        program.setProgramName("Bachelor of Science in Information Technology");
        program.setDepartment(department);
        program.setDegreeType(DegreeType.BACHELOR);
        program.setProgramDuration(4);
        program.setStatus(ActiveStatus.ACTIVE);
        program = programRepository.save(program);

        courseOne = course("FIN101-" + suffix, "Intro to Computing", department);
        courseTwo = course("FIN102-" + suffix, "Programming", department);
        facultyOne = faculty("FIN-A-" + suffix, "Ada", "Lovelace", department);
        facultyTwo = faculty("FIN-B-" + suffix, "Grace", "Hopper", department);
        roomOne = room("FIN-LAB-A-" + suffix);
        roomTwo = room("FIN-LAB-B-" + suffix);

        schoolYear = new SchoolYear();
        schoolYear.setSchoolYear("FIN-2026-" + suffix);
        schoolYear.setActive(true);
        schoolYear = schoolYearRepository.save(schoolYear);

        semester = new Semester();
        semester.setName("FIN-TERM-" + suffix);
        semester.setSortOrder(1);
        semester.setActive(true);
        semester = semesterRepository.save(semester);

        section = new Section();
        section.setSectionCode("FIN-1A-" + suffix);
        section.setProgram(program);
        section.setSchoolYear(schoolYear);
        section.setSemester(semester);
        section.setYearLevel(1);
        section.setStatus(ActiveStatus.ACTIVE);
        section = sectionRepository.save(section);

        Curriculum curriculum = new Curriculum();
        curriculum.setProgram(program);
        curriculum.setCurriculumCode("FIN-CUR-" + suffix);
        curriculum.setCurriculumName("Finance Test Curriculum");
        curriculum.setEffectiveSchoolYear("2026-2027");
        curriculum.setVersion("1");
        curriculum.setStatus(CurriculumStatus.ACTIVE);
        curriculum = curriculumRepository.save(curriculum);
        curriculumCourse(curriculum, courseOne, 1);
        curriculumCourse(curriculum, courseTwo, 2);

        student = new Student();
        student.setStudentNumber("FIN-S-" + suffix);
        student.setFirstName("Finance");
        student.setLastName("Student");
        student.setGender(Gender.OTHER);
        student.setBirthdate(LocalDate.of(2005, 1, 1));
        student.setStatus(StudentStatus.ACTIVE);
        student.setProgram(program);
        student.setCurriculum(curriculum);
        student.setYearLevel(1);
        student.setSemester(semester.getName());
        student.setDateAdmitted(LocalDate.of(2026, 6, 1));
        student.setSchoolYearAdmitted("2026-2027");
        student.setClassification(StudentClassification.REGULAR);
        student.setAcademicStatus(AcademicStatus.REGULAR);
        student = studentRepository.save(student);
    }

    @Test
    void createsFeeWithScopedRules() {
        FeeResponse fee = createPerUnitFee("TUITION-" + suffix, "100.00");

        assertThat(fee.feeCode()).startsWith("TUITION-");
        assertThat(fee.rules()).hasSize(1);
        assertThat(fee.rules().getFirst().programId()).isEqualTo(program.getId());
        assertThat(fee.rules().getFirst().schoolYearId()).isEqualTo(schoolYear.getId());
        assertThat(fee.rules().getFirst().semesterId()).isEqualTo(semester.getId());
        assertThat(fee.rules().getFirst().yearLevel()).isEqualTo(1);
    }

    @Test
    void generatesAssessmentFromConfirmedEnrollment() {
        createPerUnitFee("TUITION-" + suffix, "100.00");
        createFixedFee("MISC-" + suffix, "500.00");
        EnrollmentResponse enrollment = confirmedEnrollmentWithTwoSubjects();

        AssessmentResponse assessment = feeService.generateAssessment(enrollment.id());

        assertThat(assessment.status()).isEqualTo(AssessmentStatus.DRAFT);
        assertThat(assessment.totalUnits()).isEqualByComparingTo("6");
        assertThat(assessment.items()).hasSize(2);
        assertThat(assessment.totalAmount()).isEqualByComparingTo("1100.00");
        assertThat(assessment.items()).extracting("feeCode")
                .containsExactly("MISC-" + suffix, "TUITION-" + suffix);
    }

    @Test
    void rejectsAssessmentForDraftEnrollment() {
        createPerUnitFee("TUITION-" + suffix, "100.00");
        EnrollmentResponse enrollment = enrollmentService.create(enrollmentRequest());

        assertThatThrownBy(() -> feeService.generateAssessment(enrollment.id()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Only confirmed enrollments can generate assessments");
    }

    @Test
    void rejectsDuplicateActiveAssessment() {
        createPerUnitFee("TUITION-" + suffix, "100.00");
        EnrollmentResponse enrollment = confirmedEnrollmentWithOneSubject();
        feeService.generateAssessment(enrollment.id());

        assertThatThrownBy(() -> feeService.generateAssessment(enrollment.id()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Enrollment already has an active assessment");
    }

    @Test
    void recalculatesUnpaidAssessmentFromCurrentFeeRules() {
        FeeResponse fee = createPerUnitFee("TUITION-" + suffix, "100.00");
        EnrollmentResponse enrollment = confirmedEnrollmentWithOneSubject();
        AssessmentResponse assessment = feeService.generateAssessment(enrollment.id());

        feeService.updateFee(fee.id(), new FeeRequest(
                fee.feeCode(),
                fee.feeName(),
                fee.description(),
                ActiveStatus.ACTIVE,
                List.of(rule(FeeRuleType.PER_UNIT, "150.00", program.getId(), schoolYear.getId(), semester.getId(), 1))
        ));
        AssessmentResponse recalculated = feeService.recalculateAssessment(assessment.id());

        assertThat(recalculated.totalUnits()).isEqualByComparingTo("3");
        assertThat(recalculated.totalAmount()).isEqualByComparingTo("450.00");
        assertThat(recalculated.items().getFirst().unitAmount()).isEqualByComparingTo("150.00");
    }

    @Test
    void paidAssessmentCannotBeRecalculatedOrChanged() {
        createPerUnitFee("TUITION-" + suffix, "100.00");
        EnrollmentResponse enrollment = confirmedEnrollmentWithOneSubject();
        AssessmentResponse assessment = feeService.generateAssessment(enrollment.id());

        feeService.updateAssessmentStatus(assessment.id(), new AssessmentStatusRequest(AssessmentStatus.PAID, "Paid in full"));

        assertThatThrownBy(() -> feeService.recalculateAssessment(assessment.id()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Paid or void assessments cannot be recalculated");
        assertThatThrownBy(() -> feeService.updateAssessmentStatus(assessment.id(), new AssessmentStatusRequest(AssessmentStatus.UNPAID, "Undo")))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Paid assessments cannot be changed");
    }

    @Test
    void inactiveFeeRulesAreIgnored() {
        feeService.createFee(new FeeRequest(
                "INACTIVE-" + suffix,
                "Inactive Fee",
                null,
                ActiveStatus.ACTIVE,
                List.of(new FeeRuleRequest(FeeRuleType.FIXED, BigDecimal.valueOf(100), null, null, null, null, ActiveStatus.INACTIVE))
        ));
        EnrollmentResponse enrollment = confirmedEnrollmentWithOneSubject();

        assertThatThrownBy(() -> feeService.generateAssessment(enrollment.id()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("No active fee rules found for enrollment");
    }

    private FeeResponse createPerUnitFee(String code, String amount) {
        return feeService.createFee(new FeeRequest(
                code,
                "Tuition Fee",
                "Tuition per credit unit",
                ActiveStatus.ACTIVE,
                List.of(rule(FeeRuleType.PER_UNIT, amount, program.getId(), schoolYear.getId(), semester.getId(), 1))
        ));
    }

    private FeeResponse createFixedFee(String code, String amount) {
        return feeService.createFee(new FeeRequest(
                code,
                "Miscellaneous Fee",
                "Fixed miscellaneous fee",
                ActiveStatus.ACTIVE,
                List.of(rule(FeeRuleType.FIXED, amount, null, schoolYear.getId(), semester.getId(), null))
        ));
    }

    private FeeRuleRequest rule(FeeRuleType type, String amount, UUID programId, UUID schoolYearId, UUID semesterId, Integer yearLevel) {
        return new FeeRuleRequest(type, new BigDecimal(amount), programId, schoolYearId, semesterId, yearLevel, ActiveStatus.ACTIVE);
    }

    private EnrollmentResponse confirmedEnrollmentWithOneSubject() {
        EnrollmentResponse enrollment = enrollmentService.create(enrollmentRequest());
        ScheduleResponse schedule = schedule(courseOne, facultyOne, roomOne, DayOfWeek.MONDAY, "09:00", "10:00");
        enrollmentService.addSubject(enrollment.id(), new EnrollmentSubjectRequest(schedule.id()));
        return enrollmentService.confirm(enrollment.id());
    }

    private EnrollmentResponse confirmedEnrollmentWithTwoSubjects() {
        EnrollmentResponse enrollment = enrollmentService.create(enrollmentRequest());
        ScheduleResponse first = schedule(courseOne, facultyOne, roomOne, DayOfWeek.MONDAY, "09:00", "10:00");
        ScheduleResponse second = schedule(courseTwo, facultyTwo, roomTwo, DayOfWeek.TUESDAY, "10:00", "11:00");
        enrollmentService.addSubject(enrollment.id(), new EnrollmentSubjectRequest(first.id()));
        enrollmentService.addSubject(enrollment.id(), new EnrollmentSubjectRequest(second.id()));
        return enrollmentService.confirm(enrollment.id());
    }

    private EnrollmentRequest enrollmentRequest() {
        return new EnrollmentRequest(student.getId(), schoolYear.getId(), semester.getId(), section.getId(), "Finance test enrollment");
    }

    private ScheduleResponse schedule(Course course, Faculty faculty, Room room, DayOfWeek day, String start, String end) {
        return scheduleService.create(new ScheduleRequest(
                section.getId(),
                course.getId(),
                faculty.getId(),
                room.getId(),
                schoolYear.getId(),
                semester.getId(),
                40,
                ScheduleStatus.ACTIVE,
                List.of(new ScheduleMeetingRequest(day, LocalTime.parse(start), LocalTime.parse(end)))
        ));
    }

    private Course course(String code, String title, Department department) {
        Course course = new Course();
        course.setCourseCode(code);
        course.setCourseTitle(title);
        course.setCourseType(CourseType.MAJOR);
        course.setDepartment(department);
        course.setLectureHoursPerWeek(BigDecimal.valueOf(2));
        course.setLaboratoryHoursPerWeek(BigDecimal.valueOf(3));
        course.setCreditUnits(BigDecimal.valueOf(3));
        course.setStatus(ActiveStatus.ACTIVE);
        return courseRepository.save(course);
    }

    private Faculty faculty(String employeeNumber, String firstName, String lastName, Department department) {
        Faculty faculty = new Faculty();
        faculty.setEmployeeNumber(employeeNumber);
        faculty.setFirstName(firstName);
        faculty.setLastName(lastName);
        faculty.setEmail(employeeNumber.toLowerCase() + "@sis.local");
        faculty.setDepartment(department);
        faculty.setEmploymentStatus(EmploymentStatus.FULL_TIME);
        faculty.setFacultyType(FacultyType.INSTRUCTOR);
        faculty.setStatus(ActiveStatus.ACTIVE);
        return facultyRepository.save(faculty);
    }

    private Room room(String code) {
        Room room = new Room();
        room.setRoomCode(code);
        room.setRoomName(code + " Room");
        room.setCapacity(40);
        room.setStatus(ActiveStatus.ACTIVE);
        return roomRepository.save(room);
    }

    private void curriculumCourse(Curriculum curriculum, Course course, int sortOrder) {
        CurriculumCourse curriculumCourse = new CurriculumCourse();
        curriculumCourse.setCurriculum(curriculum);
        curriculumCourse.setYearLevel(1);
        curriculumCourse.setSemester(semester.getName());
        curriculumCourse.setCourse(course);
        curriculumCourse.setSortOrder(sortOrder);
        curriculumCourse.setRequiredStatus(RequiredStatus.REQUIRED);
        curriculumCourseRepository.save(curriculumCourse);
    }
}
