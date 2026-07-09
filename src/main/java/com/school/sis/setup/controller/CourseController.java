package com.school.sis.setup.controller;

import com.school.sis.common.response.ApiResponse;
import com.school.sis.common.response.PageResponse;
import com.school.sis.setup.dto.CourseRequest;
import com.school.sis.setup.dto.CourseResponse;
import com.school.sis.setup.service.CourseService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/courses")
public class CourseController {

    private final CourseService courseService;

    public CourseController(CourseService courseService) {
        this.courseService = courseService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_VIEW')")
    public ApiResponse<PageResponse<CourseResponse>> list(@RequestParam(required = false) String search, Pageable pageable) {
        return ApiResponse.success("Courses retrieved", courseService.list(search, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_VIEW')")
    public ApiResponse<CourseResponse> get(@PathVariable UUID id) {
        return ApiResponse.success("Course retrieved", courseService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_MANAGE')")
    public ApiResponse<CourseResponse> create(@Valid @RequestBody CourseRequest request) {
        return ApiResponse.success("Course created", courseService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ACADEMIC_SETUP_MANAGE')")
    public ApiResponse<CourseResponse> update(@PathVariable UUID id, @Valid @RequestBody CourseRequest request) {
        return ApiResponse.success("Course updated", courseService.update(id, request));
    }
}
