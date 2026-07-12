package com.school.sis.schedule.controller;

import com.school.sis.common.response.ApiResponse;
import com.school.sis.common.response.PageResponse;
import com.school.sis.schedule.dto.ScheduleConflictRequest;
import com.school.sis.schedule.dto.ScheduleConflictResponse;
import com.school.sis.schedule.dto.ScheduleRequest;
import com.school.sis.schedule.dto.ScheduleResponse;
import com.school.sis.schedule.dto.ScheduleSearchCriteria;
import com.school.sis.schedule.entity.ScheduleStatus;
import com.school.sis.schedule.service.ScheduleService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.DayOfWeek;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/schedules")
public class ScheduleController {

    private final ScheduleService scheduleService;

    public ScheduleController(ScheduleService scheduleService) {
        this.scheduleService = scheduleService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SCHEDULE_VIEW')")
    public ApiResponse<PageResponse<ScheduleResponse>> list(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) UUID schoolYearId,
            @RequestParam(required = false) UUID semesterId,
            @RequestParam(required = false) UUID programId,
            @RequestParam(required = false) UUID sectionId,
            @RequestParam(required = false) UUID facultyId,
            @RequestParam(required = false) UUID roomId,
            @RequestParam(required = false) UUID courseId,
            @RequestParam(required = false) DayOfWeek dayOfWeek,
            @RequestParam(required = false) ScheduleStatus status,
            @RequestParam(required = false) UUID curriculumId,
            @RequestParam(required = false) Integer yearLevel,
            Pageable pageable
    ) {
        ScheduleSearchCriteria criteria = new ScheduleSearchCriteria(search, schoolYearId, semesterId, programId, sectionId, facultyId, roomId, courseId, dayOfWeek, status, curriculumId, yearLevel);
        return ApiResponse.success("Schedules retrieved", scheduleService.list(criteria, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SCHEDULE_MANAGE')")
    public ApiResponse<ScheduleResponse> create(@Valid @RequestBody ScheduleRequest request) {
        return ApiResponse.success("Schedule created", scheduleService.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCHEDULE_VIEW')")
    public ApiResponse<ScheduleResponse> get(@PathVariable UUID id) {
        return ApiResponse.success("Schedule retrieved", scheduleService.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SCHEDULE_MANAGE')")
    public ApiResponse<ScheduleResponse> update(@PathVariable UUID id, @Valid @RequestBody ScheduleRequest request) {
        return ApiResponse.success("Schedule updated", scheduleService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SCHEDULE_MANAGE')")
    public ApiResponse<Void> delete(@PathVariable UUID id) {
        scheduleService.delete(id);
        return ApiResponse.success("Schedule archived");
    }

    @PostMapping("/check-conflict")
    @PreAuthorize("hasAuthority('SCHEDULE_VIEW')")
    public ApiResponse<ScheduleConflictResponse> checkConflict(@Valid @RequestBody ScheduleConflictRequest request) {
        return ApiResponse.success("Schedule conflict check completed", scheduleService.checkConflict(request));
    }
}
