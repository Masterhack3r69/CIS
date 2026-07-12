package com.school.sis.grade;

import com.school.sis.auth.security.SisUserDetails;
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
import com.school.sis.enrollment.dto.EnrollmentValidationResponse;
import com.school.sis.enrollment.service.EnrollmentService;
import com.school.sis.grade.dto.GradeClassResponse;
import com.school.sis.grade.dto.GradeEncodeRequest;
import com.school.sis.grade.dto.GradeEntryRequest;
import com.school.sis.grade.entity.GradeRemark;
import com.school.sis.grade.entity.GradeStatus;
import com.school.sis.grade.service.GradeService;
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
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class GradeServiceTests {

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
    private Course prerequisiteCourse;
    private Course advancedCourse;
    private Faculty faculty;
    private Faculty otherFaculty;
    private Room roomOne;
    private Room roomTwo;
    private SchoolYear schoolYear;
    private SchoolYear nextSchoolYear;
    private Semester semester;
    private Semester nextSemester;
    private Section section;
    private Section nextSection;
    private Student student;
    private Student otherStudent;
    private Curriculum curriculum;

    @Autowired
    GradeServiceTests(
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
        department.setDepartmentCode("GRD-CCS-" + suffix);
        department.setDepartmentName("Grade Department " + suffix);
        department.setStatus(ActiveStatus.ACTIVE);
        department = departmentRepository.save(department);

        program = new Program();
        program.setProgramCode("GRD-BSIT-" + suffix);
        program.setProgramName("Grade Program");
        program.setDepartment(department);
        program.setDegreeType(DegreeType.BACHELOR);
        program.setProgramDuration(4);
        program.setStatus(ActiveStatus.ACTIVE);
        program = programRepository.save(program);

        prerequisiteCourse = course("GRD-101-" + suffix, "Prerequisite Course", department);
        advancedCourse = course("GRD-201-" + suffix, "Advanced Course", department);
        faculty = faculty("GRD-F1-" + suffix, "Assigned", department);
        otherFaculty = faculty("GRD-F2-" + suffix, "Other", department);
        roomOne = room("GRD-R1-" + suffix);
        roomTwo = room("GRD-R2-" + suffix);
        schoolYear = schoolYear("GRD-2026-" + suffix);
        nextSchoolYear = schoolYear("GRD-2027-" + suffix);
        semester = semester("GRDFIRST" + suffix, 1);
        nextSemester = semester("GRDSECOND" + suffix, 2);

        curriculum = new Curriculum();
        curriculum.setProgram(program);
        curriculum.setCurriculumCode("GRD-CUR-" + suffix);
        curriculum.setCurriculumName("Grade Curriculum");
        curriculum.setEffectiveSchoolYear("2026-2027");
        curriculum.setVersion("1");
        curriculum.setStatus(CurriculumStatus.ACTIVE);
        curriculum = curriculumRepository.save(curriculum);
        curriculumCourse(prerequisiteCourse, 1);
        CurriculumCourse advanced = new CurriculumCourse();
        advanced.setCurriculum(curriculum);
        advanced.setYearLevel(1);
        advanced.setSemester(nextSemester.getName());
        advanced.setCourse(advancedCourse);
        advanced.setSortOrder(2);
        advanced.setRequiredStatus(RequiredStatus.REQUIRED);
        advanced = curriculumCourseRepository.save(advanced);
        advanced.getPrerequisites().add(prerequisiteCourse);
        curriculumCourseRepository.save(advanced);

        section = section("GRD-1A-" + suffix, schoolYear, semester);
        nextSection = section("GRD-1B-" + suffix, nextSchoolYear, nextSemester);

        student = student("GRD-S1-" + suffix, "First");
        otherStudent = student("GRD-S2-" + suffix, "Second");
    }

    @Test
    void createsDraftRowsForConfirmedEnrollmentSubjects() {
        ScheduleResponse schedule = confirmedClassWithTwoStudents();

        GradeClassResponse response = gradeService.classGrades(schedule.id(), facultyUser(faculty));

        assertThat(response.grades()).hasSize(2);
        assertThat(response.grades()).extracting("status").containsOnly(GradeStatus.DRAFT);
    }

    @Test
    void facultyCanEncodeAssignedClassButNotAnotherClass() {
        ScheduleResponse schedule = confirmedClassWithTwoStudents();
        GradeClassResponse classGrades = gradeService.classGrades(schedule.id(), facultyUser(faculty));

        GradeClassResponse encoded = gradeService.encode(schedule.id(), new GradeEncodeRequest(List.of(
                new GradeEntryRequest(classGrades.grades().getFirst().enrollmentSubjectId(), new BigDecimal("1.25"), null)
        )), facultyUser(faculty));

        assertThat(encoded.grades().getFirst().status()).isEqualTo(GradeStatus.ENCODED);
        assertThatThrownBy(() -> gradeService.encode(schedule.id(), new GradeEncodeRequest(List.of(
                new GradeEntryRequest(classGrades.grades().getFirst().enrollmentSubjectId(), new BigDecimal("1.50"), null)
        )), facultyUser(otherFaculty)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Faculty can only encode assigned classes");
    }

    @Test
    void rejectsInvalidGradeValuesAndIncrements() {
        ScheduleResponse schedule = confirmedClassWithTwoStudents();
        GradeClassResponse classGrades = gradeService.classGrades(schedule.id(), facultyUser(faculty));

        assertThatThrownBy(() -> gradeService.encode(schedule.id(), new GradeEncodeRequest(List.of(
                new GradeEntryRequest(classGrades.grades().getFirst().enrollmentSubjectId(), new BigDecimal("0.75"), null)
        )), facultyUser(faculty)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Final grade must be between 1.00 and 5.00");

        assertThatThrownBy(() -> gradeService.encode(schedule.id(), new GradeEncodeRequest(List.of(
                new GradeEntryRequest(classGrades.grades().getFirst().enrollmentSubjectId(), new BigDecimal("1.10"), null)
        )), facultyUser(faculty)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Final grade must use 0.25 increments");
    }

    @Test
    void submitRequiresAllClassGrades() {
        ScheduleResponse schedule = confirmedClassWithTwoStudents();
        GradeClassResponse classGrades = gradeService.classGrades(schedule.id(), facultyUser(faculty));
        gradeService.encode(schedule.id(), new GradeEncodeRequest(List.of(
                new GradeEntryRequest(classGrades.grades().getFirst().enrollmentSubjectId(), new BigDecimal("1.25"), null)
        )), facultyUser(faculty));

        assertThatThrownBy(() -> gradeService.submit(schedule.id(), facultyUser(faculty)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("All class grades must be encoded before submission");
    }

    @Test
    void submittedGradesCanBeApprovedLockedAndCreateAcademicRecords() {
        ScheduleResponse schedule = confirmedClassWithTwoStudents();
        encodeAll(schedule, "1.25");

        GradeClassResponse submitted = gradeService.submit(schedule.id(), facultyUser(faculty));
        GradeClassResponse approved = gradeService.approve(schedule.id(), approverUser());
        GradeClassResponse locked = gradeService.lock(schedule.id(), approverUser());

        assertThat(submitted.grades()).extracting("status").containsOnly(GradeStatus.SUBMITTED);
        assertThat(approved.grades()).extracting("status").containsOnly(GradeStatus.APPROVED);
        assertThat(locked.grades()).extracting("status").containsOnly(GradeStatus.LOCKED);
        assertThat(gradeService.academicRecords(student.getId())).hasSize(1);
        assertThat(gradeService.academicRecords(student.getId()).getFirst().earnedUnits()).isEqualByComparingTo("3");

        assertThatThrownBy(() -> gradeService.encode(schedule.id(), new GradeEncodeRequest(List.of(
                new GradeEntryRequest(locked.grades().getFirst().enrollmentSubjectId(), new BigDecimal("1.50"), null)
        )), facultyUser(faculty)))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Submitted, approved, or locked grades cannot be edited");
    }

    @Test
    void lockedPassingGradeSatisfiesPrerequisiteAndFailedGradeDoesNot() {
        ScheduleResponse prerequisiteSchedule = confirmedPrerequisiteForStudent(student, "2.00", DayOfWeek.WEDNESDAY, "13:00", "14:00");
        gradeService.lock(prerequisiteSchedule.id(), approverUser());
        EnrollmentResponse advancedEnrollment = enrollment(student, nextSchoolYear, nextSemester, nextSection);
        ScheduleResponse advancedSchedule = schedule(advancedCourse, nextSection, roomTwo, DayOfWeek.TUESDAY, "10:00", "11:00");
        enrollmentService.addSubject(advancedEnrollment.id(), new EnrollmentSubjectRequest(advancedSchedule.id()));

        EnrollmentValidationResponse valid = enrollmentService.validate(advancedEnrollment.id());

        assertThat(valid.valid()).isTrue();

        Student failingStudent = student("GRD-SF-" + UUID.randomUUID().toString().substring(0, 6), "Failing");
        ScheduleResponse failingPrerequisiteSchedule = confirmedPrerequisiteForStudent(failingStudent, "5.00", DayOfWeek.THURSDAY, "13:00", "14:00");
        gradeService.lock(failingPrerequisiteSchedule.id(), approverUser());
        EnrollmentResponse blockedEnrollment = enrollment(failingStudent, nextSchoolYear, nextSemester, nextSection);
        enrollmentService.addSubject(blockedEnrollment.id(), new EnrollmentSubjectRequest(advancedSchedule.id()));

        EnrollmentValidationResponse blocked = enrollmentService.validate(blockedEnrollment.id());

        assertThat(blocked.valid()).isFalse();
        assertThat(blocked.blockingIssues()).extracting("code").contains("PREREQUISITE_NOT_SATISFIED");
    }

    private ScheduleResponse confirmedClassWithTwoStudents() {
        ScheduleResponse schedule = schedule(prerequisiteCourse, section, roomOne, DayOfWeek.MONDAY, "09:00", "10:00");
        confirmStudentInSchedule(student, schoolYear, semester, section, schedule);
        confirmStudentInSchedule(otherStudent, schoolYear, semester, section, schedule);
        return schedule;
    }

    private ScheduleResponse confirmedPrerequisiteForStudent(Student targetStudent, String gradeValue, DayOfWeek day, String start, String end) {
        ScheduleResponse schedule = schedule(prerequisiteCourse, section, roomOne, day, start, end);
        confirmStudentInSchedule(targetStudent, schoolYear, semester, section, schedule);
        encodeAll(schedule, gradeValue);
        gradeService.submit(schedule.id(), facultyUser(faculty));
        gradeService.approve(schedule.id(), approverUser());
        return schedule;
    }

    private void encodeAll(ScheduleResponse schedule, String gradeValue) {
        GradeClassResponse classGrades = gradeService.classGrades(schedule.id(), facultyUser(faculty));
        gradeService.encode(schedule.id(), new GradeEncodeRequest(classGrades.grades().stream()
                .map(grade -> new GradeEntryRequest(grade.enrollmentSubjectId(), new BigDecimal(gradeValue), null))
                .toList()), facultyUser(faculty));
    }

    private void confirmStudentInSchedule(Student targetStudent, SchoolYear targetYear, Semester targetSemester, Section targetSection, ScheduleResponse schedule) {
        EnrollmentResponse enrollment = enrollment(targetStudent, targetYear, targetSemester, targetSection);
        enrollmentService.addSubject(enrollment.id(), new EnrollmentSubjectRequest(schedule.id()));
        enrollmentService.confirm(enrollment.id());
    }

    private EnrollmentResponse enrollment(Student targetStudent, SchoolYear targetYear, Semester targetSemester, Section targetSection) {
        return enrollmentService.create(new EnrollmentRequest(targetStudent.getId(), targetYear.getId(), targetSemester.getId(), targetStudent.getYearLevel(), targetSection.getId(), null));
    }

    private ScheduleResponse schedule(Course course, Section targetSection, Room room, DayOfWeek day, String start, String end) {
        return scheduleService.create(new ScheduleRequest(
                targetSection.getId(),
                course.getId(),
                faculty.getId(),
                room.getId(),
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
        course.setLectureHoursPerWeek(BigDecimal.valueOf(3));
        course.setLaboratoryHoursPerWeek(BigDecimal.ZERO);
        course.setCreditUnits(BigDecimal.valueOf(3));
        course.setStatus(ActiveStatus.ACTIVE);
        return courseRepository.save(course);
    }

    private Faculty faculty(String employeeNumber, String firstName, Department department) {
        Faculty faculty = new Faculty();
        faculty.setEmployeeNumber(employeeNumber);
        faculty.setFirstName(firstName);
        faculty.setLastName("Faculty");
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

    private SchoolYear schoolYear(String value) {
        SchoolYear schoolYear = new SchoolYear();
        schoolYear.setSchoolYear(value);
        schoolYear.setActive(true);
        return schoolYearRepository.save(schoolYear);
    }

    private Semester semester(String name, int sortOrder) {
        Semester semester = new Semester();
        semester.setName(name);
        semester.setSortOrder(sortOrder);
        semester.setActive(true);
        return semesterRepository.save(semester);
    }

    private Section section(String code, SchoolYear targetYear, Semester targetSemester) {
        Section section = new Section();
        section.setSectionCode(code);
        section.setProgram(program);
        section.setCurriculum(curriculum);
        section.setSchoolYear(targetYear);
        section.setSemester(targetSemester);
        section.setYearLevel(1);
        section.setStatus(ActiveStatus.ACTIVE);
        return sectionRepository.save(section);
    }

    private Student student(String number, String firstName) {
        Student student = new Student();
        student.setStudentNumber(number);
        student.setFirstName(firstName);
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
        return studentRepository.save(student);
    }

    private CurriculumCourse curriculumCourse(Course course, int sortOrder) {
        CurriculumCourse curriculumCourse = new CurriculumCourse();
        curriculumCourse.setCurriculum(curriculum);
        curriculumCourse.setYearLevel(1);
        curriculumCourse.setSemester(semester.getName());
        curriculumCourse.setCourse(course);
        curriculumCourse.setSortOrder(sortOrder);
        curriculumCourse.setRequiredStatus(RequiredStatus.REQUIRED);
        return curriculumCourseRepository.save(curriculumCourse);
    }

    private SisUserDetails facultyUser(Faculty userFaculty) {
        SisUserDetails userDetails = mock(SisUserDetails.class);
        when(userDetails.id()).thenReturn(UUID.randomUUID());
        when(userDetails.facultyId()).thenReturn(userFaculty.getId());
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("GRADE_ENCODE"));
        doReturn(authorities).when(userDetails).getAuthorities();
        return userDetails;
    }

    private SisUserDetails approverUser() {
        SisUserDetails userDetails = mock(SisUserDetails.class);
        when(userDetails.id()).thenReturn(UUID.randomUUID());
        when(userDetails.facultyId()).thenReturn(null);
        Collection<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("GRADE_APPROVE"));
        doReturn(authorities).when(userDetails).getAuthorities();
        return userDetails;
    }
}
