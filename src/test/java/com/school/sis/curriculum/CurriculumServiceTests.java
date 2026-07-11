package com.school.sis.curriculum;

import com.school.sis.common.response.PageResponse;
import com.school.sis.curriculum.dto.CurriculumRequest;
import com.school.sis.curriculum.dto.CurriculumResponse;
import com.school.sis.curriculum.dto.CurriculumDetailResponse;
import com.school.sis.curriculum.entity.CurriculumStatus;
import com.school.sis.curriculum.service.CurriculumService;
import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.entity.DegreeType;
import com.school.sis.setup.entity.Department;
import com.school.sis.setup.entity.Program;
import com.school.sis.setup.repository.DepartmentRepository;
import com.school.sis.setup.repository.ProgramRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class CurriculumServiceTests {

    private final CurriculumService curriculumService;
    private final DepartmentRepository departmentRepository;
    private final ProgramRepository programRepository;

    private Program program;

    @Autowired
    CurriculumServiceTests(
            CurriculumService curriculumService,
            DepartmentRepository departmentRepository,
            ProgramRepository programRepository
    ) {
        this.curriculumService = curriculumService;
        this.departmentRepository = departmentRepository;
        this.programRepository = programRepository;
    }

    @BeforeEach
    void setUp() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        Department department = new Department();
        department.setDepartmentCode("DEPT-" + suffix);
        department.setDepartmentName("Department " + suffix);
        department.setStatus(ActiveStatus.ACTIVE);
        department = departmentRepository.save(department);

        program = new Program();
        program.setProgramCode("PROG-" + suffix);
        program.setProgramName("Program " + suffix);
        program.setDepartment(department);
        program.setDegreeType(DegreeType.BACHELOR);
        program.setStatus(ActiveStatus.ACTIVE);
        program = programRepository.save(program);
    }

    @Test
    void testCreateCurriculum() {
        CurriculumRequest request = new CurriculumRequest(
                program.getId(),
                "CURR-CS-2026",
                "Computer Science Curriculum 2026",
                "2026-2027",
                "1.0",
                CurriculumStatus.DRAFT,
                "Initial Draft"
        );

        CurriculumResponse response = curriculumService.create(request);

        assertThat(response.id()).isNotNull();
        assertThat(response.curriculumCode()).isEqualTo("CURR-CS-2026");
        assertThat(response.curriculumName()).isEqualTo("Computer Science Curriculum 2026");
        assertThat(response.effectiveSchoolYear()).isEqualTo("2026-2027");
        assertThat(response.version()).isEqualTo("1.0");
        assertThat(response.status()).isEqualTo(CurriculumStatus.DRAFT);
    }

    @Test
    void testUpdateCurriculum() {
        CurriculumRequest createRequest = new CurriculumRequest(
                program.getId(),
                "CURR-CS-ORIG",
                "Computer Science Original",
                "2026-2027",
                "1.0",
                CurriculumStatus.DRAFT,
                "Original description"
        );

        CurriculumResponse created = curriculumService.create(createRequest);

        CurriculumRequest updateRequest = new CurriculumRequest(
                program.getId(),
                "CURR-CS-UPD",
                "Computer Science Updated",
                "2026-2027",
                "1.1",
                CurriculumStatus.DRAFT,
                "Updated description"
        );

        CurriculumResponse updated = curriculumService.update(created.id(), updateRequest);

        assertThat(updated.id()).isEqualTo(created.id());
        assertThat(updated.curriculumCode()).isEqualTo("CURR-CS-UPD");
        assertThat(updated.curriculumName()).isEqualTo("Computer Science Updated");
        assertThat(updated.version()).isEqualTo("1.1");
    }

    @Test
    void testActivateCurriculum() {
        // Create first curriculum and set it to ACTIVE
        CurriculumRequest req1 = new CurriculumRequest(
                program.getId(),
                "CURR-1",
                "Curriculum 1",
                "2026-2027",
                "1.0",
                CurriculumStatus.ACTIVE,
                "Curriculum 1"
        );
        CurriculumResponse res1 = curriculumService.create(req1);

        // Create second curriculum in DRAFT
        CurriculumRequest req2 = new CurriculumRequest(
                program.getId(),
                "CURR-2",
                "Curriculum 2",
                "2026-2027",
                "1.0",
                CurriculumStatus.DRAFT,
                "Curriculum 2"
        );
        CurriculumResponse res2 = curriculumService.create(req2);

        // Verify status
        assertThat(curriculumService.get(res1.id()).curriculum().status()).isEqualTo(CurriculumStatus.ACTIVE);
        assertThat(curriculumService.get(res2.id()).curriculum().status()).isEqualTo(CurriculumStatus.DRAFT);

        // Activate the second curriculum
        CurriculumResponse activatedRes = curriculumService.activate(res2.id());

        // Verify second curriculum is now ACTIVE
        assertThat(activatedRes.status()).isEqualTo(CurriculumStatus.ACTIVE);
        assertThat(curriculumService.get(res2.id()).curriculum().status()).isEqualTo(CurriculumStatus.ACTIVE);

        // Verify first curriculum is now INACTIVE
        assertThat(curriculumService.get(res1.id()).curriculum().status()).isEqualTo(CurriculumStatus.INACTIVE);
    }

    @Test
    void testGetAndListCurricula() {
        CurriculumRequest request = new CurriculumRequest(
                program.getId(),
                "CURR-CS-LIST",
                "Curriculum cs list",
                "2026-2027",
                "1.0",
                CurriculumStatus.DRAFT,
                "For testing list"
        );
        CurriculumResponse created = curriculumService.create(request);

        // Test Get
        CurriculumDetailResponse detail = curriculumService.get(created.id());
        assertThat(detail.curriculum().curriculumCode()).isEqualTo("CURR-CS-LIST");

        // Test List
        PageResponse<CurriculumResponse> page = curriculumService.list("CURR-CS", PageRequest.of(0, 10));
        assertThat(page.items()).isNotEmpty();
        assertThat(page.items().stream().anyMatch(c -> c.id().equals(created.id()))).isTrue();
    }
}
