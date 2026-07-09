package com.school.sis.grade;

import com.school.sis.auth.entity.User;
import com.school.sis.auth.repository.UserRepository;
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
import com.school.sis.enrollment.service.EnrollmentService;
import com.school.sis.grade.dto.AcademicRecordResponse;
import com.school.sis.grade.dto.ClassGradeSheetResponse;
import com.school.sis.grade.dto.GradeEncodeItemRequest;
import com.school.sis.grade.dto.GradeEncodeRequest;
import com.school.sis.grade.entity.GradeRemark;
import com.school.sis.grade.entity.GradeStatus;
import com.school.sis.grade.entity.SpecialGrade;
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
import com.school.sis.student.dto.StudentAcademicRecordsResponse;
import com.school.sis.student.entity.AcademicStatus;
import com.school.sis.student.entity.Gender;
import com.school.sis.student.entity.Student;
import com.school.sis.student.entity.StudentClassification;
import com.school.sis.student.entity.StudentStatus;
import com.school.sis.student.repository.StudentRepository;
import com.school.sis.student.service.StudentService;
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
class GradeServiceTests {

    private final GradeService gradeService;
    private final EnrollmentService enrollmentService;
    private final ScheduleService scheduleService;
    private final StudentService studentService;
    private final UserRepository userRepository;
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

    private String suffix;
    private Program program;
    private Course course;
    private Faculty faculty;
    private Faculty unlinkedFaculty;
    private Room room;
    private SchoolYear schoolYear;
    private Semester semester;
    private Section section;
    private Curriculum curriculum;
    private User facultyUser;
    private User otherUser;
    private SisUserDetails facultyDetails;
    private SisUserDetails otherDetails;

    @Autowired
    GradeServiceTests(
            GradeService gradeService,
            EnrollmentService enrollmentService,
            ScheduleService scheduleService,
            StudentService studentService,
            UserRepository userRepository,
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
        this.studentService = studentService;
        this.userRepository = userRepository;
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
        facultyUser = user("faculty-" + suffix, "faculty-" + suffix + "@sis.local", "Faculty User");
        otherUser = user("other-" + suffix, "other-" + suffix + "@sis.local", "Other User");
        facultyDetails = new SisUserDetails(facultyUser);
        otherDetails = new SisUserDetails(otherUser);

        Department department = new Department();
        department.setDepartmentCode("GRD-CCS-" + suffix);
        department.setDepartmentName("Grade Test College " + suffix);
        department.setStatus(ActiveStatus.ACTIVE);
        department = departmentRepository.save(department);

        program = new Program();
        program.setProgramCode("GRD-BSIT-" + suffix);
        program.setProgramName("Bachelor of Science in Information Technology");
        program.setDepartment(department);
        program.setDegreeType(DegreeType.BACHELOR);
        program.setProgramDuration(4);
        program.setStatus(ActiveStatus.ACTIVE);
        program = programRepository.save(program);

        course = course("GRD101-" + suffix, "Grade Testing", department);
        faculty = faculty("GRD-FAC-" + suffix, "Ada", "Lovelace", department, facultyUser);
        unlinkedFaculty = faculty("GRD-UNL-" + suffix, "No", "User", department, null);
        room = room("GRD-RM-" + suffix);

        schoolYear = new SchoolYear();
        schoolYear.setSchoolYear("GRD-2026-" + suffix);
        schoolYear.setActive(true);
        schoolYear = schoolYearRepository.save(schoolYear);

        semester = new Semester();
        semester.setName("GRD-TERM-" + suffix);
        semester.setSortOrder(1);
        semester.setActive(true);
        semester = semesterRepository.save(semester);

        section = new Section();
        section.setSectionCode("GRD-1A-" + suffix);
        section.setProgram(program);
        section.setSchoolYear(schoolYear);
        section.setSemester(semester);
        section.setYearLevel(1);
        section.setStatus(ActiveStatus.ACTIVE);
        section = sectionRepository.save(section);

        curriculum = new Curriculum();
        curriculum.setProgram(program);
        curriculum.setCurriculumCode("GRD-CUR-" + suffix);
        curriculum.setCurriculumName("Grade Test Curriculum");
        curriculum.setEffectiveSchoolYear("2026-2027");
        curriculum.setVersion("1");
        curriculum.setStatus(CurriculumStatus.ACTIVE);
        curriculum = curriculumRepository.save(curriculum);
        curriculumCourse(curriculum, course, 1);
    }

    @Test
    void assignedFacultyCanEncodeAndSubmitGrades() {
        Student student = student("S1");
        ScheduleResponse schedule = schedule(faculty);
        EnrollmentResponse enrollment = confirmedEnrollment(student, schedule);

        ClassGradeSheetResponse encoded = gradeService.encode(schedule.id(), encodeRequest(enrollment.subjects().getFirst().id(), "1.25"), facultyDetails);
        ClassGradeSheetResponse submitted = gradeService.submit(schedule.id(), facultyDetails);

        assertThat(encoded.rows().getFirst().grade().remark()).isEqualTo(GradeRemark.PASSED);
        assertThat(submitted.rows().getFirst().grade().status()).isEqualTo(GradeStatus.SUBMITTED);
    }

    @Test
    void mismatchedOrUnlinkedFacultyCannotEncode() {
        Student student = student("S1");
        ScheduleResponse linkedSchedule = schedule(faculty);
        EnrollmentResponse linkedEnrollment = confirmedEnrollment(student, linkedSchedule);

        assertThatThrownBy(() -> gradeService.encode(linkedSchedule.id(), encodeRequest(linkedEnrollment.subjects().getFirst().id(), "1.25"), otherDetails))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Only the assigned faculty user can encode grades for this class");

        Student otherStudent = student("S2");
        ScheduleResponse unlinkedSchedule = schedule(unlinkedFaculty, DayOfWeek.TUESDAY);
        EnrollmentResponse unlinkedEnrollment = confirmedEnrollment(otherStudent, unlinkedSchedule);

        assertThatThrownBy(() -> gradeService.encode(unlinkedSchedule.id(), encodeRequest(unlinkedEnrollment.subjects().getFirst().id(), "1.25"), facultyDetails))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Only the assigned faculty user can encode grades for this class");
    }

    @Test
    void rejectsInvalidGradeValues() {
        Student student = student("S1");
        ScheduleResponse schedule = schedule(faculty);
        EnrollmentResponse enrollment = confirmedEnrollment(student, schedule);
        UUID subjectId = enrollment.subjects().getFirst().id();

        assertThatThrownBy(() -> gradeService.encode(schedule.id(), encodeRequest(subjectId, "0.75"), facultyDetails))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Numeric grade must be between 1.00 and 5.00");
        assertThatThrownBy(() -> gradeService.encode(schedule.id(), encodeRequest(subjectId, "1.10"), facultyDetails))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Numeric grade must use 0.25 increments");
        assertThatThrownBy(() -> gradeService.encode(schedule.id(), new GradeEncodeRequest(List.of(
                new GradeEncodeItemRequest(subjectId, BigDecimal.valueOf(1.25), SpecialGrade.INC, null)
        )), facultyDetails))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Exactly one grade value is required");
    }

    @Test
    void encodesFailedAndSpecialGradesWithDerivedRemarks() {
        Student failedStudent = student("S1");
        Student incompleteStudent = student("S2");
        ScheduleResponse schedule = schedule(faculty);
        EnrollmentResponse failedEnrollment = confirmedEnrollment(failedStudent, schedule);
        EnrollmentResponse incompleteEnrollment = confirmedEnrollment(incompleteStudent, schedule);

        ClassGradeSheetResponse sheet = gradeService.encode(schedule.id(), new GradeEncodeRequest(List.of(
                new GradeEncodeItemRequest(failedEnrollment.subjects().getFirst().id(), BigDecimal.valueOf(3.25), null, null),
                new GradeEncodeItemRequest(incompleteEnrollment.subjects().getFirst().id(), null, SpecialGrade.INC, null)
        )), facultyDetails);

        assertThat(sheet.rows()).extracting(row -> row.grade().remark())
                .containsExactlyInAnyOrder(GradeRemark.FAILED, GradeRemark.INCOMPLETE);
    }

    @Test
    void submitRejectsIncompleteClassSheet() {
        Student first = student("S1");
        Student second = student("S2");
        ScheduleResponse schedule = schedule(faculty);
        EnrollmentResponse firstEnrollment = confirmedEnrollment(first, schedule);
        confirmedEnrollment(second, schedule);
        gradeService.encode(schedule.id(), encodeRequest(firstEnrollment.subjects().getFirst().id(), "1.25"), facultyDetails);

        assertThatThrownBy(() -> gradeService.submit(schedule.id(), facultyDetails))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("All enrolled students must have encoded grades");
    }

    @Test
    void reviewApproveAndLockCreatesAcademicRecords() {
        Student student = student("S1");
        ScheduleResponse schedule = schedule(faculty);
        EnrollmentResponse enrollment = confirmedEnrollment(student, schedule);
        gradeService.encode(schedule.id(), encodeRequest(enrollment.subjects().getFirst().id(), "2.00"), facultyDetails);
        gradeService.submit(schedule.id(), facultyDetails);
        gradeService.review(schedule.id(), otherDetails);
        ClassGradeSheetResponse approved = gradeService.approve(schedule.id(), otherDetails);
        ClassGradeSheetResponse locked = gradeService.lock(schedule.id(), otherDetails);

        StudentAcademicRecordsResponse records = studentService.academicRecords(student.getId());
        AcademicRecordResponse record = records.records().getFirst();

        assertThat(approved.rows().getFirst().grade().status()).isEqualTo(GradeStatus.APPROVED);
        assertThat(locked.rows().getFirst().grade().status()).isEqualTo(GradeStatus.LOCKED);
        assertThat(record.gradeStatus()).isEqualTo(GradeStatus.LOCKED);
        assertThat(record.earnedUnits()).isEqualByComparingTo("3");
    }

    @Test
    void lockedGradesCannotBeEdited() {
        Student student = student("S1");
        ScheduleResponse schedule = schedule(faculty);
        EnrollmentResponse enrollment = confirmedEnrollment(student, schedule);
        UUID subjectId = enrollment.subjects().getFirst().id();
        gradeService.encode(schedule.id(), encodeRequest(subjectId, "2.00"), facultyDetails);
        gradeService.submit(schedule.id(), facultyDetails);
        gradeService.review(schedule.id(), otherDetails);
        gradeService.approve(schedule.id(), otherDetails);
        gradeService.lock(schedule.id(), otherDetails);

        assertThatThrownBy(() -> gradeService.encode(schedule.id(), encodeRequest(subjectId, "1.00"), facultyDetails))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Only encoded or returned grades can be updated");
    }

    @Test
    void failedAndSpecialGradesDoNotEarnUnits() {
        Student failedStudent = student("S1");
        Student incompleteStudent = student("S2");
        ScheduleResponse schedule = schedule(faculty);
        EnrollmentResponse failedEnrollment = confirmedEnrollment(failedStudent, schedule);
        EnrollmentResponse incompleteEnrollment = confirmedEnrollment(incompleteStudent, schedule);
        gradeService.encode(schedule.id(), new GradeEncodeRequest(List.of(
                new GradeEncodeItemRequest(failedEnrollment.subjects().getFirst().id(), BigDecimal.valueOf(5.00), null, null),
                new GradeEncodeItemRequest(incompleteEnrollment.subjects().getFirst().id(), null, SpecialGrade.NG, null)
        )), facultyDetails);
        gradeService.submit(schedule.id(), facultyDetails);
        gradeService.review(schedule.id(), otherDetails);
        gradeService.approve(schedule.id(), otherDetails);
        gradeService.lock(schedule.id(), otherDetails);

        assertThat(studentService.academicRecords(failedStudent.getId()).records().getFirst().earnedUnits()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(studentService.academicRecords(incompleteStudent.getId()).records().getFirst().earnedUnits()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    private User user(String username, String email, String fullName) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPasswordHash("test-password-hash");
        user.setActive(true);
        return userRepository.save(user);
    }

    private Student student(String marker) {
        Student student = new Student();
        student.setStudentNumber("GRD-" + marker + "-" + suffix);
        student.setFirstName("Grade");
        student.setLastName("Student " + marker);
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
        return studentRepository.save(student);
    }

    private EnrollmentResponse confirmedEnrollment(Student student, ScheduleResponse schedule) {
        EnrollmentResponse enrollment = enrollmentService.create(new EnrollmentRequest(student.getId(), schoolYear.getId(), semester.getId(), section.getId(), "Grade test"));
        enrollmentService.addSubject(enrollment.id(), new EnrollmentSubjectRequest(schedule.id()));
        return enrollmentService.confirm(enrollment.id());
    }

    private GradeEncodeRequest encodeRequest(UUID enrollmentSubjectId, String finalGrade) {
        return new GradeEncodeRequest(List.of(new GradeEncodeItemRequest(enrollmentSubjectId, new BigDecimal(finalGrade), null, null)));
    }

    private ScheduleResponse schedule(Faculty faculty) {
        return schedule(faculty, DayOfWeek.MONDAY);
    }

    private ScheduleResponse schedule(Faculty faculty, DayOfWeek day) {
        return scheduleService.create(new ScheduleRequest(
                section.getId(),
                course.getId(),
                faculty.getId(),
                room.getId(),
                schoolYear.getId(),
                semester.getId(),
                40,
                ScheduleStatus.ACTIVE,
                List.of(new ScheduleMeetingRequest(day, LocalTime.parse("09:00"), LocalTime.parse("10:00")))
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

    private Faculty faculty(String employeeNumber, String firstName, String lastName, Department department, User user) {
        Faculty faculty = new Faculty();
        faculty.setEmployeeNumber(employeeNumber);
        faculty.setFirstName(firstName);
        faculty.setLastName(lastName);
        faculty.setEmail(employeeNumber.toLowerCase() + "@sis.local");
        faculty.setDepartment(department);
        faculty.setEmploymentStatus(EmploymentStatus.FULL_TIME);
        faculty.setFacultyType(FacultyType.INSTRUCTOR);
        faculty.setUser(user);
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
