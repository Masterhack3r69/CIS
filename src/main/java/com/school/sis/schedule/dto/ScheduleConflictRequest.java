package com.school.sis.schedule.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ScheduleConflictRequest(
        UUID ignoreScheduleId,
        @NotNull UUID sectionId,
        @NotNull UUID facultyId,
        @NotNull UUID roomId,
        @NotNull UUID schoolYearId,
        @NotNull UUID semesterId,
        @NotNull @Size(min = 1) List<@Valid ScheduleMeetingRequest> meetings
) {
}
