package com.school.sis.schedule.repository;

import com.school.sis.schedule.entity.ClassSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.UUID;

public interface ClassScheduleRepository extends JpaRepository<ClassSchedule, UUID>, JpaSpecificationExecutor<ClassSchedule> {
    java.util.List<ClassSchedule> findBySectionIdAndSchoolYearIdAndSemesterIdAndStatus(
            UUID sectionId, UUID schoolYearId, UUID semesterId, com.school.sis.schedule.entity.ScheduleStatus status);
}
