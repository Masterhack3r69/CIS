package com.school.sis.schedule;

import com.school.sis.common.exception.BusinessRuleException;
import com.school.sis.schedule.dto.ScheduleConflictRequest;
import com.school.sis.schedule.dto.ScheduleConflictResponse;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ScheduleServiceTests {

    private final ScheduleService scheduleService;
    private final DepartmentRepository departmentRepository;
    private final ProgramRepository programRepository;
    private final CourseRepository courseRepository;
    private final FacultyRepository facultyRepository;
    private final RoomRepository roomRepository;
    private final SchoolYearRepository schoolYearRepository;
    private final SemesterRepository semesterRepository;
    private final SectionRepository sectionRepository;

    private Section section;
    private Section otherSection;
    private Course course;
    private Faculty faculty;
    private Faculty otherFaculty;
    private Room room;
    private Room otherRoom;
    private SchoolYear schoolYear;
    private Semester semester;

    @Autowired
    ScheduleServiceTests(
            ScheduleService scheduleService,
            DepartmentRepository departmentRepository,
            ProgramRepository programRepository,
            CourseRepository courseRepository,
            FacultyRepository facultyRepository,
            RoomRepository roomRepository,
            SchoolYearRepository schoolYearRepository,
            SemesterRepository semesterRepository,
            SectionRepository sectionRepository
    ) {
        this.scheduleService = scheduleService;
        this.departmentRepository = departmentRepository;
        this.programRepository = programRepository;
        this.courseRepository = courseRepository;
        this.facultyRepository = facultyRepository;
        this.roomRepository = roomRepository;
        this.schoolYearRepository = schoolYearRepository;
        this.semesterRepository = semesterRepository;
        this.sectionRepository = sectionRepository;
    }

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        Department department = new Department();
        department.setDepartmentCode("CCS-" + suffix);
        department.setDepartmentName("College of Computer Studies " + suffix);
        department.setStatus(ActiveStatus.ACTIVE);
        department = departmentRepository.save(department);

        Program program = new Program();
        program.setProgramCode("BSIT-" + suffix);
        program.setProgramName("Bachelor of Science in Information Technology");
        program.setDepartment(department);
        program.setDegreeType(DegreeType.BACHELOR);
        program.setProgramDuration(4);
        program.setStatus(ActiveStatus.ACTIVE);
        program = programRepository.save(program);

        course = new Course();
        course.setCourseCode("CCS-" + suffix);
        course.setCourseTitle("Fundamentals of Programming");
        course.setCourseType(CourseType.MAJOR);
        course.setDepartment(department);
        course.setLectureHoursPerWeek(BigDecimal.valueOf(2));
        course.setLaboratoryHoursPerWeek(BigDecimal.valueOf(3));
        course.setCreditUnits(BigDecimal.valueOf(3));
        course.setStatus(ActiveStatus.ACTIVE);
        course = courseRepository.save(course);

        faculty = faculty("EMP-A-" + suffix, "Ada", "Lovelace", department);
        otherFaculty = faculty("EMP-B-" + suffix, "Grace", "Hopper", department);
        room = room("LAB-A-" + suffix);
        otherRoom = room("LAB-B-" + suffix);

        schoolYear = new SchoolYear();
        schoolYear.setSchoolYear("2026-" + suffix);
        schoolYear.setActive(true);
        schoolYear = schoolYearRepository.save(schoolYear);

        semester = new Semester();
        semester.setName("TERM-" + suffix);
        semester.setSortOrder(1);
        semester.setActive(true);
        semester = semesterRepository.save(semester);

        section = section("BSIT-1A-" + suffix, program);
        otherSection = section("BSIT-1B-" + suffix, program);
    }

    @Test
    void checkConflictReportsRoomFacultyAndSectionOverlaps() {
        ScheduleResponse existing = scheduleService.create(request(
                section.getId(),
                faculty.getId(),
                room.getId(),
                ScheduleStatus.ACTIVE,
                meeting(DayOfWeek.MONDAY, "09:00", "10:30")
        ));

        ScheduleConflictResponse response = scheduleService.checkConflict(new ScheduleConflictRequest(
                null,
                section.getId(),
                faculty.getId(),
                room.getId(),
                schoolYear.getId(),
                semester.getId(),
                List.of(meeting(DayOfWeek.MONDAY, "10:00", "11:00"))
        ));

        assertThat(response.hasConflicts()).isTrue();
        assertThat(response.conflicts())
                .extracting("conflictType")
                .containsExactlyInAnyOrder("ROOM", "FACULTY", "SECTION");
        assertThat(response.conflicts())
                .allSatisfy(conflict -> assertThat(conflict.scheduleId()).isEqualTo(existing.id()));
    }

    @Test
    void activeScheduleRejectsOverlappingRoomConflict() {
        scheduleService.create(request(
                section.getId(),
                faculty.getId(),
                room.getId(),
                ScheduleStatus.ACTIVE,
                meeting(DayOfWeek.TUESDAY, "09:00", "10:00")
        ));

        assertThatThrownBy(() -> scheduleService.create(request(
                otherSection.getId(),
                otherFaculty.getId(),
                room.getId(),
                ScheduleStatus.ACTIVE,
                meeting(DayOfWeek.TUESDAY, "09:30", "10:30")
        )))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Schedule has conflicts");
    }

    @Test
    void backToBackMeetingsAreAllowed() {
        scheduleService.create(request(
                section.getId(),
                faculty.getId(),
                room.getId(),
                ScheduleStatus.ACTIVE,
                meeting(DayOfWeek.WEDNESDAY, "09:00", "10:00")
        ));

        ScheduleResponse response = scheduleService.create(request(
                otherSection.getId(),
                otherFaculty.getId(),
                room.getId(),
                ScheduleStatus.ACTIVE,
                meeting(DayOfWeek.WEDNESDAY, "10:00", "11:00")
        ));

        assertThat(response.id()).isNotNull();
    }

    @Test
    void updateIgnoresCurrentScheduleDuringConflictCheck() {
        ScheduleResponse existing = scheduleService.create(request(
                section.getId(),
                faculty.getId(),
                room.getId(),
                ScheduleStatus.ACTIVE,
                meeting(DayOfWeek.THURSDAY, "13:00", "14:30")
        ));

        ScheduleResponse updated = scheduleService.update(existing.id(), request(
                section.getId(),
                faculty.getId(),
                room.getId(),
                ScheduleStatus.ACTIVE,
                meeting(DayOfWeek.THURSDAY, "13:00", "14:30")
        ));

        assertThat(updated.id()).isEqualTo(existing.id());
    }

    @Test
    void invalidTimeRangeIsRejected() {
        assertThatThrownBy(() -> scheduleService.create(request(
                section.getId(),
                faculty.getId(),
                room.getId(),
                ScheduleStatus.ACTIVE,
                meeting(DayOfWeek.FRIDAY, "15:00", "15:00")
        )))
                .isInstanceOf(BusinessRuleException.class)
                .hasMessage("Meeting end time must be after start time");
    }

    private ScheduleRequest request(UUID sectionId, UUID facultyId, UUID roomId, ScheduleStatus status, ScheduleMeetingRequest meeting) {
        return new ScheduleRequest(
                sectionId,
                course.getId(),
                facultyId,
                roomId,
                schoolYear.getId(),
                semester.getId(),
                40,
                status,
                List.of(meeting)
        );
    }

    private ScheduleMeetingRequest meeting(DayOfWeek dayOfWeek, String startTime, String endTime) {
        return new ScheduleMeetingRequest(dayOfWeek, LocalTime.parse(startTime), LocalTime.parse(endTime));
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

    private Room room(String roomCode) {
        Room room = new Room();
        room.setRoomCode(roomCode);
        room.setRoomName(roomCode + " Room");
        room.setCapacity(40);
        room.setStatus(ActiveStatus.ACTIVE);
        return roomRepository.save(room);
    }

    private Section section(String sectionCode, Program program) {
        Section section = new Section();
        section.setSectionCode(sectionCode);
        section.setProgram(program);
        section.setSchoolYear(schoolYear);
        section.setSemester(semester);
        section.setYearLevel(1);
        section.setStatus(ActiveStatus.ACTIVE);
        return sectionRepository.save(section);
    }
}
