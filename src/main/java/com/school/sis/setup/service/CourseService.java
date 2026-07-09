package com.school.sis.setup.service;

import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.response.PageResponse;
import com.school.sis.setup.dto.CourseRequest;
import com.school.sis.setup.dto.CourseResponse;
import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.entity.Course;
import com.school.sis.setup.repository.CourseRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class CourseService {

    private final CourseRepository courseRepository;
    private final DepartmentService departmentService;

    public CourseService(CourseRepository courseRepository, DepartmentService departmentService) {
        this.courseRepository = courseRepository;
        this.departmentService = departmentService;
    }

    @Transactional(readOnly = true)
    public PageResponse<CourseResponse> list(String search, Pageable pageable) {
        String term = search == null ? "" : search;
        return PageResponse.from(courseRepository
                .findByCourseCodeContainingIgnoreCaseOrCourseTitleContainingIgnoreCase(term, term, pageable)
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public CourseResponse get(UUID id) {
        return toResponse(find(id));
    }

    @Transactional
    public CourseResponse create(CourseRequest request) {
        Course course = new Course();
        apply(course, request);
        return toResponse(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse update(UUID id, CourseRequest request) {
        Course course = find(id);
        apply(course, request);
        return toResponse(course);
    }

    Course find(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course not found"));
    }

    private void apply(Course course, CourseRequest request) {
        course.setCourseCode(request.courseCode());
        course.setCourseTitle(request.courseTitle());
        course.setCourseDescription(request.courseDescription());
        course.setLectureHoursPerWeek(request.lectureHoursPerWeek());
        course.setLaboratoryHoursPerWeek(request.laboratoryHoursPerWeek());
        course.setCreditUnits(request.creditUnits());
        course.setCourseType(request.courseType());
        course.setDepartment(departmentService.find(request.departmentId()));
        course.setStatus(request.status() == null ? ActiveStatus.ACTIVE : request.status());
    }

    private CourseResponse toResponse(Course course) {
        return new CourseResponse(
                course.getId(),
                course.getCourseCode(),
                course.getCourseTitle(),
                course.getCourseDescription(),
                course.getLectureHoursPerWeek(),
                course.getLaboratoryHoursPerWeek(),
                course.getCreditUnits(),
                course.getCourseType(),
                course.getDepartment().getId(),
                course.getDepartment().getDepartmentCode(),
                course.getStatus()
        );
    }
}
