package com.school.sis.enrollment.dto;

import com.school.sis.enrollment.entity.EnrollmentSubjectStatus;
import com.school.sis.schedule.dto.ScheduleMeetingResponse;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record EnrollmentSubjectResponse(
        UUID id,
        UUID scheduleId,
        UUID courseId,
        String courseCode,
        String courseTitle,
        BigDecimal creditUnits,
        UUID sectionId,
        String sectionCode,
        UUID facultyId,
        String facultyName,
        UUID roomId,
        String roomCode,
        EnrollmentSubjectStatus status,
        List<ScheduleMeetingResponse> meetings
) {
}
