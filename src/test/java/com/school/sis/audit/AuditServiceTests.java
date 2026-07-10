package com.school.sis.audit;

import com.school.sis.audit.dto.AuditLogSearchCriteria;
import com.school.sis.audit.repository.AuditLogRepository;
import com.school.sis.audit.service.AuditService;
import com.school.sis.auth.entity.User;
import com.school.sis.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.BeanUtils;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@SpringBootTest
class AuditServiceTests {

    private final AuditService auditService;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;

    @Autowired
    AuditServiceTests(AuditService auditService, AuditLogRepository auditLogRepository, UserRepository userRepository) {
        this.auditService = auditService;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
    }

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
    }

    @Test
    void searchFiltersByModuleActionEntityAndDateRange() throws InterruptedException {
        UUID targetId = UUID.randomUUID();
        auditService.log("STUDENT_CREATED", "STUDENT", "Student", targetId, null,
                Map.of("studentNumber", "S-100"));
        Thread.sleep(5);
        Instant dateFrom = Instant.now();
        Thread.sleep(5);
        auditService.log("GRADE_LOCKED", "GRADE", "ClassSchedule", UUID.randomUUID(), null,
                Map.of("gradeCount", 3));

        var byModuleAction = auditService.search(new AuditLogSearchCriteria(
                "STUDENT", "STUDENT_CREATED", null, null, null, null, null
        ), PageRequest.of(0, 10));
        var byEntity = auditService.search(new AuditLogSearchCriteria(
                null, null, null, "Student", targetId, null, null
        ), PageRequest.of(0, 10));
        var byDate = auditService.search(new AuditLogSearchCriteria(
                null, null, null, null, null, dateFrom, null
        ), PageRequest.of(0, 10));

        assertThat(byModuleAction.totalElements()).isEqualTo(1);
        assertThat(byModuleAction.items().getFirst().newValue().get("studentNumber").asText()).isEqualTo("S-100");
        assertThat(byEntity.totalElements()).isEqualTo(1);
        assertThat(byDate.totalElements()).isEqualTo(1);
        assertThat(byDate.items().getFirst().action()).isEqualTo("GRADE_LOCKED");
    }

    @Test
    void searchFiltersByUserIdAndDefaultsNewestFirst() {
        User user = userRepository.save(testUser());
        auditService.log(user, "LOGIN_SUCCESS", "AUTH", "User", user.getId(), null,
                Map.of("username", user.getUsername()));
        auditService.log("LOGIN_FAILED", "AUTH", "User", null, null,
                Map.of("usernameOrEmail", "missing"));

        var byUser = auditService.search(new AuditLogSearchCriteria(
                null, null, user.getId(), null, null, null, null
        ), PageRequest.of(0, 10));
        var newestFirst = auditService.search(new AuditLogSearchCriteria(
                null, null, null, null, null, null, null
        ), PageRequest.of(0, 10));

        assertThat(byUser.totalElements()).isEqualTo(1);
        assertThat(byUser.items().getFirst().userId()).isEqualTo(user.getId());
        assertThat(newestFirst.items()).hasSize(2);
        assertThat(newestFirst.items().get(0).createdAt()).isAfterOrEqualTo(newestFirst.items().get(1).createdAt());
    }

    @Test
    void explicitSortIsRespected() {
        auditService.log("A_FIRST", "TEST", "Entity", UUID.randomUUID(), null, Map.of("value", 1));
        auditService.log("Z_SECOND", "TEST", "Entity", UUID.randomUUID(), null, Map.of("value", 2));

        var response = auditService.search(new AuditLogSearchCriteria(
                null, null, null, null, null, null, null
        ), PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "action")));

        assertThat(response.items().getFirst().action()).isEqualTo("A_FIRST");
    }

    private User testUser() {
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        User user = BeanUtils.instantiateClass(User.class);
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(user, "email", "audit-" + suffix + "@sis.local");
        ReflectionTestUtils.setField(user, "username", "audit-" + suffix);
        ReflectionTestUtils.setField(user, "passwordHash", "{noop}secret");
        ReflectionTestUtils.setField(user, "fullName", "Audit User");
        ReflectionTestUtils.setField(user, "active", true);
        return user;
    }
}
