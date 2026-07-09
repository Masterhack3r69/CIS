package com.school.sis.schedule.dto;

import com.school.sis.schedule.entity.ScheduleStatus;

import java.util.List;
import java.util.UUID;

public record ScheduleResponse(
        UUID id,
        UUID sectionId,
        String sectionCode,
        UUID courseId,
        String courseCode,
        String courseTitle,
        UUID facultyId,
        String facultyName,
        UUID roomId,
        String roomCode,
        UUID schoolYearId,
        String schoolYear,
        UUID semesterId,
        String semesterName,
        Integer capacity,
        ScheduleStatus status,
        List<ScheduleMeetingResponse> meetings
) {
}
