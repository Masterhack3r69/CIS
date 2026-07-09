package com.school.sis.curriculum.controller;

import com.school.sis.common.response.ApiResponse;
import com.school.sis.common.response.PageResponse;
import com.school.sis.curriculum.dto.CurriculumChecklistResponse;
import com.school.sis.curriculum.dto.CurriculumCourseRequest;
import com.school.sis.curriculum.dto.CurriculumCourseResponse;
import com.school.sis.curriculum.dto.CurriculumDetailResponse;
import com.school.sis.curriculum.dto.CurriculumRequest;
import com.school.sis.curriculum.dto.CurriculumResponse;
import com.school.sis.curriculum.service.CurriculumService;
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

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/curricula")
public class CurriculumController {

    private final CurriculumService curriculumService;

    public CurriculumController(CurriculumService curriculumService) {
        this.curriculumService = curriculumService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CURRICULUM_VIEW')")
    public ApiResponse<PageResponse<CurriculumResponse>> list(@RequestParam(required = false) String search, Pageable pageable) {
        return ApiResponse.success("Curricula retrieved", curriculumService.list(search, pageable));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('CURRICULUM_MANAGE')")
    public ApiResponse<CurriculumResponse> create(@Valid @RequestBody CurriculumRequest request) {
        return ApiResponse.success("Curriculum created", curriculumService.create(request));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('CURRICULUM_VIEW')")
    public ApiResponse<CurriculumDetailResponse> get(@PathVariable UUID id) {
        return ApiResponse.success("Curriculum retrieved", curriculumService.get(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('CURRICULUM_MANAGE')")
    public ApiResponse<CurriculumResponse> update(@PathVariable UUID id, @Valid @RequestBody CurriculumRequest request) {
        return ApiResponse.success("Curriculum updated", curriculumService.update(id, request));
    }

    @PostMapping("/{id}/courses")
    @PreAuthorize("hasAuthority('CURRICULUM_MANAGE')")
    public ApiResponse<CurriculumCourseResponse> addCourse(@PathVariable UUID id, @Valid @RequestBody CurriculumCourseRequest request) {
        return ApiResponse.success("Curriculum course added", curriculumService.addCourse(id, request));
    }

    @PutMapping("/{id}/courses/{curriculumCourseId}")
    @PreAuthorize("hasAuthority('CURRICULUM_MANAGE')")
    public ApiResponse<CurriculumCourseResponse> updateCourse(
            @PathVariable UUID id,
            @PathVariable UUID curriculumCourseId,
            @Valid @RequestBody CurriculumCourseRequest request
    ) {
        return ApiResponse.success("Curriculum course updated", curriculumService.updateCourse(id, curriculumCourseId, request));
    }

    @DeleteMapping("/{id}/courses/{curriculumCourseId}")
    @PreAuthorize("hasAuthority('CURRICULUM_MANAGE')")
    public ApiResponse<Void> deleteCourse(@PathVariable UUID id, @PathVariable UUID curriculumCourseId) {
        curriculumService.deleteCourse(id, curriculumCourseId);
        return ApiResponse.success("Curriculum course deleted");
    }

    @GetMapping("/{id}/checklist")
    @PreAuthorize("hasAuthority('CURRICULUM_VIEW')")
    public ApiResponse<CurriculumChecklistResponse> checklist(@PathVariable UUID id) {
        return ApiResponse.success("Curriculum checklist retrieved", curriculumService.checklist(id));
    }

    @PostMapping("/{id}/activate")
    @PreAuthorize("hasAuthority('CURRICULUM_MANAGE')")
    public ApiResponse<CurriculumResponse> activate(@PathVariable UUID id) {
        return ApiResponse.success("Curriculum activated", curriculumService.activate(id));
    }
}
