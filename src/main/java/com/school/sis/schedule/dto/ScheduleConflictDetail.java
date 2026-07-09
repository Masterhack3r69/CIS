package com.school.sis.schedule.dto;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.UUID;

public record ScheduleConflictDetail(
        String conflictType,
        UUID scheduleId,
        String courseCode,
        String courseTitle,
        String sectionCode,
        String facultyName,
        String roomCode,
        DayOfWeek dayOfWeek,
        LocalTime existingStartTime,
        LocalTime existingEndTime,
        LocalTime requestedStartTime,
        LocalTime requestedEndTime
) {
}
