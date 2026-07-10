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
import com.school.sis.enrollment.entity.EnrollmentStatus;
import com.school.sis.enrollment.service.EnrollmentService;
import com.school.sis.fee.dto.AssessmentResponse;
import com.school.sis.fee.dto.AssessmentStatusRequest;
import com.school.sis.fee.dto.FeeItemRequest;
import com.school.sis.fee.dto.FeeRuleRequest;
import com.school.sis.fee.entity.AssessmentStatus;
import com.school.sis.fee.entity.FeeCategory;
import com.school.sis.fee.entity.FeeComputationType;
import com.school.sis.fee.service.AssessmentService;
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
class FeeAssessmentServiceTests {

    private final FeeService feeService;
    private final AssessmentService assessmentService;
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
    private Course labCourse;
    private Course lectureCourse;
    private Faculty faculty;
    private Room roomOne;
    private Room roomTwo;
    private SchoolYear schoolYear;
    private Semester semester;
    private Section section;
    private Student student;

    @Autowired
    FeeAssessmentServiceTests(
            FeeService feeService,
            AssessmentService assessmentService,
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
        this.assessmentService = assessmentService;
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
        department.setDepartmentCode("FEE-CCS-" + suffix);
        department.setDepartmentName("Fee Test Department " + suffix);
        department.setStatus(ActiveStatus.ACTIVE);
        department = departmentRepository.save(department);

        program = new Program();
        program.setProgramCode("FEE-BSIT-" + suffix);
        program.setProgramName("Fee Test Program");
        program.setDepartment(department);
        program.setDegreeType(DegreeType.BACHELOR);
        program.setProgramDuration(4);
        program.setStatus(ActiveStatus.ACTIVE);
        program = programRepository.save(program);

        labCourse = course("FEE-LAB-" + suffix, "Lab Course", department, "3", "3");
        lectureCourse = course("FEE-LEC-" + suffix, "Lecture Course", department, "3", "0");

        faculty = new Faculty();
        faculty.setEmployeeNumber("FEE-EMP-" + suffix);
        faculty.setFirstName("Fee");
        faculty.setLastName("Faculty");
        faculty.setEmail("fee-" + suffix + "@sis.local");
        faculty.setDepartment(department);
        faculty.setEmploymentStatus(EmploymentStatus.FULL_TIME);
        faculty.setFacultyType(FacultyType.INSTRUCTOR);
        faculty.setStatus(ActiveStatus.ACTIVE);
        faculty = facultyRepository.save(faculty);

        roomOne = room("FEE-R1-" + suffix);
        roomTwo = room("FEE-R2-" + suffix);

        schoolYear = new SchoolYear();
        schoolYear.setSchoolYear("FEE-2026-" + suffix);
        schoolYear.setActive(true);
        schoolYear = schoolYearRepository.save(schoolYear);

        semester = new Semester();
        semester.setName("FEE-TERM-" + suffix);
        semester.setSortOrder(1);
        semester.setActive(true);
        semester = semesterRepository.save(semester);

        section = new Section();
        section.setSectionCode("FEE-1A-" + suffix);
        section.setProgram(program);
        section.setSchoolYear(schoolYear);
        section.setSemester(semester);
        section.setYearLevel(1);
        section.setStatus(ActiveStatus.ACTIVE);
        section = sectionRepository.save(section);

        Curriculum curriculum = new Curriculum();
        curriculum.setProgram(program);
        curriculum.setCurriculumCode("FEE-CUR-" + suffix);
        curriculum.setCurriculumName("Fee Curriculum");
        curriculum.setEffectiveSchoolYear("2026-2027");
        curriculum.setVersion("1");
        curriculum.setStatus(CurriculumStatus.ACTIVE);
        curriculum = curriculumRepository.save(curriculum);
        curriculumCourse(curriculum, labCourse, 1);
        curriculumCourse(curriculum, lectureCourse, 2);

        student = new Student();
        student.setStudentNumber("FEE-S-" + suffix);
        student.setFirstName("Fee");
        student.setLastName("Student");
        student.setGender(Gender.OTHER);
        student.setBirthdate(LocalDate.of(2005, 1, 1));
        student.setStatus(StudentStatus.ACTIVE);
        student.setProgram(program);
        student.setCurriculum(curriculum);
        student.setYearLevel(1);
        student.setSemester(semester.getName());
        student.setSection(section);
        student.setDateAdmitted(LocalDate.of(2026, 6, 1));
        student.setSchoolYearAdmitted("2026-2027");
        student.setClassification(StudentClassification.REGULAR);
        student.setAcademicStatus(AcademicStatus.REGULAR);
        student = studentRepository.save(student);
    }

    @Test
    void generatesAssessmentFromApplicableFeeRules() {
        createFee("TUITION", FeeCategory.TUITION, FeeComputationType.PER_UNIT, "100");
        createFee("LAB", FeeCategory.LABORATORY, FeeComputationType.PER_LABORATORY_SUBJECT, "500");
        createFee("MISC", FeeCategory.MISCELLANEOUS, FeeComputationType.FIXED_AMOUNT, "1000");
        EnrollmentResponse enrollment = enrollmentWithTwoSubjects();

        AssessmentResponse assessment = assessmentService.generate(enrollment.id());

        assertThat(assessment.totalUnits()).isEqualByComparingTo("6");
        assertThat(assessment.tuitionAmount()).isEqualByComparingTo("600");
        assertThat(assessment.laboratoryFeeAmount()).isEqualByComparingTo("500");
        assertThat(assessment.miscellaneousFeeAmount()).isEqualByComparingTo("1000");
        assertThat(assessment.totalAssessment()).isEqualByComparingTo("2100");
        assertThat(assessment.balance()).isEqualByComparingTo("2100");
        assertThat(assessment.items()).hasSize(3);
    }

    @Test
    void ignoresInactiveAndNonMatchingRules() {
        createFee("ACTIVE", FeeCategory.MISCELLANEOUS, FeeComputationType.FIXED_AMOUNT, "300");
        createFee("INACTIVE", FeeCategory.MISCELLANEOUS, FeeComputationType.FIXED_AMOUNT, "999", ActiveStatus.INACTIVE, 1);
        createFee("WRONG_YEAR", FeeCategory.MISCELLANEOUS, FeeComputationType.FIXED_AMOUNT, "999", ActiveStatus.ACTIVE, 2);
        EnrollmentResponse enrollment = enrollmentWithTwoSubjects();

        AssessmentResponse assessment = assessmentService.generate(enrollment.id());

        assertThat(assessment.totalAssessment()).isEqualByComparingTo("300");
        assertThat(assessment.items()).extracting("feeCode").containsExactly("ACTIVE");
    }

    @Test
    void rejectsDuplicateAssessmentForEnrollment() {
        createFee("MISC-DUP", FeeCategory.MISCELLANEOUS, FeeComputationType.FIXED_AMOUNT, "100");
        EnrollmentResponse enrollment = enrollmentWithTwoSubjects();
        assessmentService.generate(enrollment.id());

        assertThatThrownBy(() -> assessmentService.generate(enrollment.id()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Assessment already exists for this enrollment");
    }

    @Test
    void recalculatesUnpaidAssessmentFromCurrentFeeRules() {
        createFee("MISC-OLD", FeeCategory.MISCELLANEOUS, FeeComputationType.FIXED_AMOUNT, "100");
        EnrollmentResponse enrollment = enrollmentWithTwoSubjects();
        AssessmentResponse original = assessmentService.generate(enrollment.id());
        createFee("MISC-NEW", FeeCategory.MISCELLANEOUS, FeeComputationType.FIXED_AMOUNT, "250");

        AssessmentResponse recalculated = assessmentService.recalculate(original.id());

        assertThat(recalculated.totalAssessment()).isEqualByComparingTo("350");
        assertThat(recalculated.items()).hasSize(2);
    }

    @Test
    void blocksRecalculationAfterPayment() {
        createFee("MISC-PAID", FeeCategory.MISCELLANEOUS, FeeComputationType.FIXED_AMOUNT, "100");
        AssessmentResponse assessment = assessmentService.generate(enrollmentWithTwoSubjects().id());
        assessmentService.updateStatus(assessment.id(), new AssessmentStatusRequest(AssessmentStatus.PARTIAL, BigDecimal.valueOf(50)));

        assertThatThrownBy(() -> assessmentService.recalculate(assessment.id()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Only unpaid active assessments can be recalculated");
    }

    @Test
    void rejectsCancelledEnrollmentAssessment() {
        createFee("MISC-CANCEL", FeeCategory.MISCELLANEOUS, FeeComputationType.FIXED_AMOUNT, "100");
        EnrollmentResponse enrollment = enrollmentWithTwoSubjects();
        enrollmentService.confirm(enrollment.id());
        EnrollmentResponse cancelled = enrollmentService.cancel(enrollment.id());

        assertThat(cancelled.status()).isEqualTo(EnrollmentStatus.CANCELLED);
        assertThatThrownBy(() -> assessmentService.generate(enrollment.id()))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Cancelled enrollments cannot be assessed");
    }

    private EnrollmentResponse enrollmentWithTwoSubjects() {
        EnrollmentResponse enrollment = enrollmentService.create(new EnrollmentRequest(student.getId(), schoolYear.getId(), semester.getId(), section.getId(), null));
        ScheduleResponse first = schedule(labCourse, roomOne, DayOfWeek.MONDAY, "09:00", "10:00");
        ScheduleResponse second = schedule(lectureCourse, roomTwo, DayOfWeek.TUESDAY, "09:00", "10:00");
        enrollmentService.addSubject(enrollment.id(), new EnrollmentSubjectRequest(first.id()));
        return enrollmentService.addSubject(enrollment.id(), new EnrollmentSubjectRequest(second.id()));
    }

    private void createFee(String code, FeeCategory category, FeeComputationType computationType, String amount) {
        createFee(code, category, computationType, amount, ActiveStatus.ACTIVE, 1);
    }

    private void createFee(String code, FeeCategory category, FeeComputationType computationType, String amount, ActiveStatus status, int yearLevel) {
        feeService.create(new FeeItemRequest(
                code,
                code + " Fee",
                category,
                null,
                ActiveStatus.ACTIVE,
                List.of(new FeeRuleRequest(null, schoolYear.getId(), semester.getId(), program.getId(), yearLevel, computationType, new BigDecimal(amount), status))
        ));
    }

    private ScheduleResponse schedule(Course course, Room room, DayOfWeek day, String start, String end) {
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

    private Course course(String code, String title, Department department, String units, String labHours) {
        Course course = new Course();
        course.setCourseCode(code);
        course.setCourseTitle(title);
        course.setCourseType(CourseType.MAJOR);
        course.setDepartment(department);
        course.setLectureHoursPerWeek(BigDecimal.valueOf(3));
        course.setLaboratoryHoursPerWeek(new BigDecimal(labHours));
        course.setCreditUnits(new BigDecimal(units));
        course.setStatus(ActiveStatus.ACTIVE);
        return courseRepository.save(course);
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
