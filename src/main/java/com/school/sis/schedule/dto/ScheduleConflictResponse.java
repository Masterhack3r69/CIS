package com.school.sis.schedule.dto;

import java.util.List;

public record ScheduleConflictResponse(
        boolean hasConflicts,
        List<ScheduleConflictDetail> conflicts
) {
}
