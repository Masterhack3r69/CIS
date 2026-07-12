package com.school.sis.setup;

import com.school.sis.common.exception.BusinessRuleException;
import com.school.sis.curriculum.entity.Curriculum;
import com.school.sis.curriculum.entity.CurriculumStatus;
import com.school.sis.curriculum.repository.CurriculumRepository;
import com.school.sis.setup.dto.SectionRequest;
import com.school.sis.setup.entity.*;
import com.school.sis.setup.repository.*;
import com.school.sis.setup.service.SectionService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
public class SectionDuplicateCodeTests {

    @Autowired
    private SectionService sectionService;

    @Autowired
    private EntityManager entityManager;



    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private ProgramRepository programRepository;

    @Autowired
    private CurriculumRepository curriculumRepository;

    @Autowired
    private SchoolYearRepository schoolYearRepository;

    @Autowired
    private SemesterRepository semesterRepository;



    @Test
    void rejectsDuplicateSectionCodeInSameTerm() {
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

        Curriculum curriculum = new Curriculum();
        curriculum.setProgram(program);
        curriculum.setCurriculumCode("CUR-" + suffix);
        curriculum.setCurriculumName("BSIT Curriculum");
        curriculum.setEffectiveSchoolYear("2026-2027");
        curriculum.setVersion("1");
        curriculum.setStatus(CurriculumStatus.ACTIVE);
        curriculum = curriculumRepository.save(curriculum);

        SchoolYear schoolYear = new SchoolYear();
        schoolYear.setSchoolYear("2026-" + suffix);
        schoolYear.setActive(true);
        schoolYear = schoolYearRepository.save(schoolYear);

        Semester semester = new Semester();
        semester.setName("TERM" + suffix);
        semester.setSortOrder(1);
        semester.setActive(true);
        semester = semesterRepository.save(semester);

        // Create the first section
        SectionRequest request1 = new SectionRequest(
                "BSIT-1A-" + suffix,
                program.getId(),
                curriculum.getId(),
                schoolYear.getId(),
                semester.getId(),
                1,
                ActiveStatus.ACTIVE
        );
        sectionService.create(request1);
        entityManager.flush();

        // Attempt to create a second section with the same section code in the same term
        SectionRequest request2 = new SectionRequest(
                "BSIT-1A-" + suffix,
                program.getId(),
                curriculum.getId(),
                schoolYear.getId(),
                semester.getId(),
                1,
                ActiveStatus.ACTIVE
        );

        // We expect a custom business rule validation exception to be thrown
        assertThatThrownBy(() -> {
            sectionService.create(request2);
            entityManager.flush();
        }).isInstanceOf(BusinessRuleException.class)
          .hasMessage("Section code already exists in this term");
    }
}
