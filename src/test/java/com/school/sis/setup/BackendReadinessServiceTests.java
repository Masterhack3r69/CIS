package com.school.sis.setup;

import com.school.sis.auth.dto.UserManagementRequest;
import com.school.sis.auth.dto.UserStatusRequest;
import com.school.sis.auth.dto.UserSummary;
import com.school.sis.auth.entity.Role;
import com.school.sis.auth.entity.User;
import com.school.sis.auth.repository.RoleRepository;
import com.school.sis.auth.repository.UserRepository;
import com.school.sis.auth.service.UserManagementService;
import com.school.sis.common.exception.BusinessRuleException;
import com.school.sis.common.exception.NotFoundException;
import com.school.sis.setup.dto.FacultyRequest;
import com.school.sis.setup.dto.FacultyResponse;
import com.school.sis.setup.dto.RoomRequest;
import com.school.sis.setup.dto.RoomResponse;
import com.school.sis.setup.dto.SchoolYearRequest;
import com.school.sis.setup.dto.SchoolYearResponse;
import com.school.sis.setup.dto.SectionRequest;
import com.school.sis.setup.dto.SectionResponse;
import com.school.sis.setup.dto.SemesterRequest;
import com.school.sis.setup.dto.SemesterResponse;
import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.entity.DegreeType;
import com.school.sis.setup.entity.Department;
import com.school.sis.setup.entity.EmploymentStatus;
import com.school.sis.setup.entity.FacultyType;
import com.school.sis.setup.entity.Program;
import com.school.sis.setup.repository.DepartmentRepository;
import com.school.sis.setup.repository.ProgramRepository;
import com.school.sis.setup.service.FacultyService;
import com.school.sis.setup.service.RoomService;
import com.school.sis.setup.service.SchoolYearService;
import com.school.sis.setup.service.SectionService;
import com.school.sis.setup.service.SemesterService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.annotation.Transactional;

import java.lang.reflect.Constructor;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class BackendReadinessServiceTests {

    private final FacultyService facultyService;
    private final RoomService roomService;
    private final SchoolYearService schoolYearService;
    private final SemesterService semesterService;
    private final SectionService sectionService;
    private final UserManagementService userManagementService;
    private final DepartmentRepository departmentRepository;
    private final ProgramRepository programRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private String suffix;
    private Department department;
    private Program program;
    private Role role;

    @Autowired
    BackendReadinessServiceTests(
            FacultyService facultyService,
            RoomService roomService,
            SchoolYearService schoolYearService,
            SemesterService semesterService,
            SectionService sectionService,
            UserManagementService userManagementService,
            DepartmentRepository departmentRepository,
            ProgramRepository programRepository,
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.facultyService = facultyService;
        this.roomService = roomService;
        this.schoolYearService = schoolYearService;
        this.semesterService = semesterService;
        this.sectionService = sectionService;
        this.userManagementService = userManagementService;
        this.departmentRepository = departmentRepository;
        this.programRepository = programRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @BeforeEach
    void setUp() throws Exception {
        suffix = UUID.randomUUID().toString().substring(0, 8);

        department = new Department();
        department.setDepartmentCode("READY-" + suffix);
        department.setDepartmentName("Readiness Department " + suffix);
        department.setStatus(ActiveStatus.ACTIVE);
        department = departmentRepository.save(department);

        program = new Program();
        program.setProgramCode("RDY-BSIT-" + suffix);
        program.setProgramName("Readiness Program");
        program.setDepartment(department);
        program.setDegreeType(DegreeType.BACHELOR);
        program.setProgramDuration(4);
        program.setStatus(ActiveStatus.ACTIVE);
        program = programRepository.save(program);

        role = newRole();
        ReflectionTestUtils.setField(role, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(role, "name", "READY_ROLE_" + suffix);
        ReflectionTestUtils.setField(role, "description", "Readiness role");
        role = roleRepository.save(role);
    }

    @Test
    void createsAndUpdatesFacultyWithOptionalUserLink() {
        User user = user("faculty-" + suffix, "faculty-" + suffix + "@sis.local");

        FacultyResponse created = facultyService.create(facultyRequest("EMP-" + suffix, user.getId()));
        FacultyResponse updated = facultyService.update(created.id(), new FacultyRequest(
                "EMP2-" + suffix,
                "Updated",
                null,
                "Faculty",
                null,
                "updated-" + suffix + "@sis.local",
                "123",
                null,
                department.getId(),
                EmploymentStatus.PART_TIME,
                FacultyType.LECTURER,
                "Software Engineering",
                ActiveStatus.ACTIVE
        ));

        assertThat(created.userId()).isEqualTo(user.getId());
        assertThat(created.username()).isEqualTo(user.getUsername());
        assertThat(updated.userId()).isNull();
        assertThat(updated.employeeNumber()).startsWith("EMP2-");
    }

    @Test
    void facultyRejectsMissingDepartmentOrUser() {
        assertThatThrownBy(() -> facultyService.create(facultyRequest("BAD-DEPT-" + suffix, null, UUID.randomUUID())))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Department not found");
        assertThatThrownBy(() -> facultyService.create(facultyRequest("BAD-USER-" + suffix, UUID.randomUUID())))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("User not found");
    }

    @Test
    void managesRoomAndStatus() {
        RoomResponse created = roomService.create(new RoomRequest("RM-" + suffix, "Room " + suffix, 40, null));
        RoomResponse updated = roomService.update(created.id(), new RoomRequest("RM2-" + suffix, "Room Two", 50, ActiveStatus.ACTIVE));
        RoomResponse inactive = roomService.updateStatus(created.id(), ActiveStatus.INACTIVE);

        assertThat(updated.capacity()).isEqualTo(50);
        assertThat(inactive.status()).isEqualTo(ActiveStatus.INACTIVE);
    }

    @Test
    void managesSchoolYearAndSemester() {
        SchoolYearResponse schoolYear = schoolYearService.create(new SchoolYearRequest("2026-" + suffix, true));
        SchoolYearResponse updatedYear = schoolYearService.update(schoolYear.id(), new SchoolYearRequest("2027-" + suffix, false));
        SemesterResponse semester = semesterService.create(new SemesterRequest("TERM-" + suffix, 4, true));
        SemesterResponse updatedSemester = semesterService.update(semester.id(), new SemesterRequest("TERM2-" + suffix, 5, false));

        assertThat(updatedYear.schoolYear()).startsWith("2027-");
        assertThat(updatedYear.active()).isFalse();
        assertThat(updatedSemester.sortOrder()).isEqualTo(5);
        assertThat(updatedSemester.active()).isFalse();
    }

    @Test
    void managesSectionAndStatus() {
        SchoolYearResponse schoolYear = schoolYearService.create(new SchoolYearRequest("2026-" + suffix, true));
        SemesterResponse semester = semesterService.create(new SemesterRequest("TERM-" + suffix, 1, true));

        SectionResponse created = sectionService.create(new SectionRequest("SEC-" + suffix, program.getId(), schoolYear.id(), semester.id(), 1, null));
        SectionResponse updated = sectionService.update(created.id(), new SectionRequest("SEC2-" + suffix, program.getId(), schoolYear.id(), semester.id(), 2, ActiveStatus.ACTIVE));
        SectionResponse inactive = sectionService.updateStatus(created.id(), ActiveStatus.INACTIVE);

        assertThat(updated.sectionCode()).startsWith("SEC2-");
        assertThat(updated.yearLevel()).isEqualTo(2);
        assertThat(inactive.status()).isEqualTo(ActiveStatus.INACTIVE);
    }

    @Test
    void sectionRejectsMissingReferences() {
        SchoolYearResponse schoolYear = schoolYearService.create(new SchoolYearRequest("2026-" + suffix, true));
        SemesterResponse semester = semesterService.create(new SemesterRequest("TERM-" + suffix, 1, true));

        assertThatThrownBy(() -> sectionService.create(new SectionRequest("SEC-" + suffix, UUID.randomUUID(), schoolYear.id(), semester.id(), 1, null)))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Program not found");
    }

    @Test
    void managesUsersRolesAndStatus() {
        UserSummary created = userManagementService.createUser(new UserManagementRequest(
                "ready-user-" + suffix,
                "ready-user-" + suffix + "@sis.local",
                "Ready User",
                true,
                "secret123",
                Set.of(role.getId())
        ));
        User saved = userRepository.findById(created.id()).orElseThrow();

        UserSummary updated = userManagementService.updateUser(created.id(), new UserManagementRequest(
                "ready-updated-" + suffix,
                "ready-updated-" + suffix + "@sis.local",
                "Ready Updated",
                true,
                null,
                Set.of(role.getId())
        ));
        UserSummary inactive = userManagementService.updateStatus(created.id(), new UserStatusRequest(false));

        assertThat(created.roles()).contains(role.getName());
        assertThat(passwordEncoder.matches("secret123", saved.getPasswordHash())).isTrue();
        assertThat(updated.username()).startsWith("ready-updated-");
        assertThat(inactive.id()).isEqualTo(created.id());
        assertThat(userRepository.findById(created.id()).orElseThrow().isActive()).isFalse();
        assertThat(userManagementService.listRoles(org.springframework.data.domain.Pageable.unpaged()).items()).extracting("name").contains(role.getName());
    }

    @Test
    void userManagementRejectsDuplicatesAndMissingRole() {
        userManagementService.createUser(new UserManagementRequest(
                "dupe-" + suffix,
                "dupe-" + suffix + "@sis.local",
                "Dupe User",
                true,
                "secret123",
                Set.of(role.getId())
        ));

        assertThatThrownBy(() -> userManagementService.createUser(new UserManagementRequest(
                "dupe-" + suffix,
                "dupe2-" + suffix + "@sis.local",
                "Duplicate Username",
                true,
                "secret123",
                Set.of(role.getId())
        ))).isInstanceOf(BusinessRuleException.class).hasMessage("Username already exists");

        assertThatThrownBy(() -> userManagementService.createUser(new UserManagementRequest(
                "missing-role-" + suffix,
                "missing-role-" + suffix + "@sis.local",
                "Missing Role",
                true,
                "secret123",
                Set.of(UUID.randomUUID())
        ))).isInstanceOf(NotFoundException.class).hasMessage("Role not found");
    }

    private FacultyRequest facultyRequest(String employeeNumber, UUID userId) {
        return facultyRequest(employeeNumber, userId, department.getId());
    }

    private FacultyRequest facultyRequest(String employeeNumber, UUID userId, UUID departmentId) {
        return new FacultyRequest(
                employeeNumber,
                "Test",
                null,
                "Faculty",
                null,
                employeeNumber.toLowerCase() + "@sis.local",
                null,
                userId,
                departmentId,
                EmploymentStatus.FULL_TIME,
                FacultyType.INSTRUCTOR,
                null,
                ActiveStatus.ACTIVE
        );
    }

    private User user(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setFullName(username);
        user.setPasswordHash("hash");
        user.setActive(true);
        return userRepository.save(user);
    }

    private Role newRole() throws Exception {
        Constructor<Role> constructor = Role.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }
}
