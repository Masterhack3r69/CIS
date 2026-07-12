package com.school.sis.schedule.dto;

import com.school.sis.schedule.entity.ScheduleStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ScheduleRequest(
        @NotNull UUID sectionId,
        @NotNull UUID courseId,
        @NotNull UUID facultyId,
        @NotNull UUID roomId,
        @NotNull @Positive Integer capacity,
        @NotNull ScheduleStatus status,
        @NotNull @Size(min = 1) List<@Valid ScheduleMeetingRequest> meetings
) {
}
