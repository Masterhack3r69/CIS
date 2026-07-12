package com.school.sis.schedule.dto;

import com.school.sis.schedule.entity.ScheduleStatus;

import java.time.DayOfWeek;
import java.util.UUID;

public record ScheduleSearchCriteria(
        String search,
        UUID schoolYearId,
        UUID semesterId,
        UUID programId,
        UUID sectionId,
        UUID facultyId,
        UUID roomId,
        UUID courseId,
        DayOfWeek dayOfWeek,
        ScheduleStatus status,
        UUID curriculumId,
        Integer yearLevel
) {
}
