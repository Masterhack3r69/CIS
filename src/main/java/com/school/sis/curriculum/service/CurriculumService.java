package com.school.sis.curriculum.service;

import com.school.sis.common.exception.BusinessRuleException;
import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.response.PageResponse;
import com.school.sis.curriculum.dto.CourseLinkResponse;
import com.school.sis.curriculum.dto.CurriculumChecklistResponse;
import com.school.sis.curriculum.dto.CurriculumCourseRequest;
import com.school.sis.curriculum.dto.CurriculumCourseResponse;
import com.school.sis.curriculum.dto.CurriculumDetailResponse;
import com.school.sis.curriculum.dto.CurriculumRequest;
import com.school.sis.curriculum.dto.CurriculumResponse;
import com.school.sis.curriculum.dto.CurriculumTermResponse;
import com.school.sis.curriculum.entity.Curriculum;
import com.school.sis.curriculum.entity.CurriculumCourse;
import com.school.sis.curriculum.entity.CurriculumStatus;
import com.school.sis.curriculum.repository.CurriculumCourseRepository;
import com.school.sis.curriculum.repository.CurriculumRepository;
import com.school.sis.setup.entity.Course;
import com.school.sis.setup.entity.Program;
import com.school.sis.setup.repository.CourseRepository;
import com.school.sis.setup.repository.ProgramRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class CurriculumService {

    private final CurriculumRepository curriculumRepository;
    private final CurriculumCourseRepository curriculumCourseRepository;
    private final ProgramRepository programRepository;
    private final CourseRepository courseRepository;

    public CurriculumService(
            CurriculumRepository curriculumRepository,
            CurriculumCourseRepository curriculumCourseRepository,
            ProgramRepository programRepository,
            CourseRepository courseRepository
    ) {
        this.curriculumRepository = curriculumRepository;
        this.curriculumCourseRepository = curriculumCourseRepository;
        this.programRepository = programRepository;
        this.courseRepository = courseRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<CurriculumResponse> list(String search, Pageable pageable) {
        String term = search == null ? "" : search;
        return PageResponse.from(curriculumRepository
                .findByCurriculumCodeContainingIgnoreCaseOrCurriculumNameContainingIgnoreCase(term, term, pageable)
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public CurriculumDetailResponse get(UUID id) {
        Curriculum curriculum = findCurriculum(id);
        return new CurriculumDetailResponse(
                toResponse(curriculum),
                curriculumCourseRepository.findByCurriculumIdOrderByYearLevelAscSemesterAscSortOrderAsc(id)
                        .stream()
                        .map(this::toCourseResponse)
                        .toList()
        );
    }

    @Transactional
    public CurriculumResponse create(CurriculumRequest request) {
        Curriculum curriculum = new Curriculum();
        apply(curriculum, request);
        return toResponse(curriculumRepository.save(curriculum));
    }

    @Transactional
    public CurriculumResponse update(UUID id, CurriculumRequest request) {
        Curriculum curriculum = findCurriculum(id);
        apply(curriculum, request);
        return toResponse(curriculum);
    }

    @Transactional
    public CurriculumCourseResponse addCourse(UUID curriculumId, CurriculumCourseRequest request) {
        Curriculum curriculum = findCurriculum(curriculumId);
        ensureNoDuplicate(curriculumId, null, request);
        CurriculumCourse curriculumCourse = new CurriculumCourse();
        curriculumCourse.setCurriculum(curriculum);
        apply(curriculumCourse, request);
        return toCourseResponse(curriculumCourseRepository.save(curriculumCourse));
    }

    @Transactional
    public CurriculumCourseResponse updateCourse(UUID curriculumId, UUID curriculumCourseId, CurriculumCourseRequest request) {
        CurriculumCourse curriculumCourse = findCurriculumCourse(curriculumId, curriculumCourseId);
        ensureNoDuplicate(curriculumId, curriculumCourseId, request);
        apply(curriculumCourse, request);
        return toCourseResponse(curriculumCourse);
    }

    @Transactional
    public void deleteCourse(UUID curriculumId, UUID curriculumCourseId) {
        CurriculumCourse curriculumCourse = findCurriculumCourse(curriculumId, curriculumCourseId);
        curriculumCourseRepository.delete(curriculumCourse);
    }

    @Transactional(readOnly = true)
    public CurriculumChecklistResponse checklist(UUID curriculumId) {
        Curriculum curriculum = findCurriculum(curriculumId);
        List<CurriculumCourseResponse> courses = curriculumCourseRepository
                .findByCurriculumIdOrderByYearLevelAscSemesterAscSortOrderAsc(curriculumId)
                .stream()
                .map(this::toCourseResponse)
                .toList();

        Map<String, List<CurriculumCourseResponse>> grouped = courses.stream()
                .collect(Collectors.groupingBy(
                        course -> course.yearLevel() + "|" + course.semester(),
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        List<CurriculumTermResponse> terms = grouped.values().stream()
                .map(termCourses -> new CurriculumTermResponse(
                        termCourses.getFirst().yearLevel(),
                        termCourses.getFirst().semester(),
                        total(termCourses, CurriculumCourseResponse::lectureHoursPerWeek),
                        total(termCourses, CurriculumCourseResponse::laboratoryHoursPerWeek),
                        total(termCourses, CurriculumCourseResponse::creditUnits),
                        termCourses
                ))
                .toList();

        return new CurriculumChecklistResponse(toResponse(curriculum), terms);
    }

    @Transactional
    public CurriculumResponse activate(UUID curriculumId) {
        Curriculum curriculum = findCurriculum(curriculumId);
        curriculumRepository.findByProgramIdAndStatus(curriculum.getProgram().getId(), CurriculumStatus.ACTIVE)
                .stream()
                .filter(active -> !active.getId().equals(curriculumId))
                .forEach(active -> active.setStatus(CurriculumStatus.INACTIVE));
        curriculumRepository.flush();
        curriculum.setStatus(CurriculumStatus.ACTIVE);
        return toResponse(curriculum);
    }

    private void apply(Curriculum curriculum, CurriculumRequest request) {
        curriculum.setProgram(programRepository.findById(request.programId())
                .orElseThrow(() -> new NotFoundException("Program not found")));
        curriculum.setCurriculumCode(request.curriculumCode());
        curriculum.setCurriculumName(request.curriculumName());
        curriculum.setEffectiveSchoolYear(request.effectiveSchoolYear());
        curriculum.setVersion(request.version());
        curriculum.setStatus(request.status());
        curriculum.setDescription(request.description());
    }

    private void apply(CurriculumCourse curriculumCourse, CurriculumCourseRequest request) {
        curriculumCourse.setYearLevel(request.yearLevel());
        curriculumCourse.setSemester(request.semester());
        curriculumCourse.setCourse(findCourse(request.courseId()));
        curriculumCourse.setSortOrder(request.sortOrder());
        curriculumCourse.setRequiredStatus(request.requiredStatus());
        curriculumCourse.setPrerequisites(resolveCourses(request.prerequisiteCourseIds()));
        curriculumCourse.setCorequisites(resolveCourses(request.corequisiteCourseIds()));
    }

    private void ensureNoDuplicate(UUID curriculumId, UUID currentCurriculumCourseId, CurriculumCourseRequest request) {
        boolean duplicate = curriculumCourseRepository.existsByCurriculumIdAndYearLevelAndSemesterIgnoreCaseAndCourseId(
                curriculumId,
                request.yearLevel(),
                request.semester(),
                request.courseId()
        );
        if (!duplicate) {
            return;
        }
        if (currentCurriculumCourseId != null) {
            CurriculumCourse current = findCurriculumCourse(curriculumId, currentCurriculumCourseId);
            if (current.getCourse().getId().equals(request.courseId())
                    && current.getYearLevel() == request.yearLevel()
                    && current.getSemester().equalsIgnoreCase(request.semester())) {
                return;
            }
        }
        throw new BusinessRuleException("Course already exists in this curriculum year level and semester");
    }

    private Curriculum findCurriculum(UUID id) {
        return curriculumRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Curriculum not found"));
    }

    private CurriculumCourse findCurriculumCourse(UUID curriculumId, UUID curriculumCourseId) {
        return curriculumCourseRepository.findByIdAndCurriculumId(curriculumCourseId, curriculumId)
                .orElseThrow(() -> new NotFoundException("Curriculum course not found"));
    }

    private Course findCourse(UUID id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Course not found"));
    }

    private Set<Course> resolveCourses(List<UUID> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) {
            return new LinkedHashSet<>();
        }
        Set<UUID> uniqueIds = new LinkedHashSet<>(courseIds);
        List<Course> courses = courseRepository.findAllById(uniqueIds);
        if (courses.size() != uniqueIds.size()) {
            throw new NotFoundException("One or more linked courses were not found");
        }
        Map<UUID, Course> byId = courses.stream().collect(Collectors.toMap(Course::getId, Function.identity()));
        return uniqueIds.stream().map(byId::get).collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private CurriculumResponse toResponse(Curriculum curriculum) {
        Program program = curriculum.getProgram();
        return new CurriculumResponse(
                curriculum.getId(),
                program.getId(),
                program.getProgramCode(),
                curriculum.getCurriculumCode(),
                curriculum.getCurriculumName(),
                curriculum.getEffectiveSchoolYear(),
                curriculum.getVersion(),
                curriculum.getStatus(),
                curriculum.getDescription()
        );
    }

    private CurriculumCourseResponse toCourseResponse(CurriculumCourse curriculumCourse) {
        Course course = curriculumCourse.getCourse();
        return new CurriculumCourseResponse(
                curriculumCourse.getId(),
                curriculumCourse.getYearLevel(),
                curriculumCourse.getSemester(),
                course.getId(),
                course.getCourseCode(),
                course.getCourseTitle(),
                course.getLectureHoursPerWeek(),
                course.getLaboratoryHoursPerWeek(),
                course.getCreditUnits(),
                curriculumCourse.getSortOrder(),
                curriculumCourse.getRequiredStatus(),
                curriculumCourse.getPrerequisites().stream()
                        .sorted(Comparator.comparing(Course::getCourseCode))
                        .map(this::toCourseLink)
                        .toList(),
                curriculumCourse.getCorequisites().stream()
                        .sorted(Comparator.comparing(Course::getCourseCode))
                        .map(this::toCourseLink)
                        .toList()
        );
    }

    private CourseLinkResponse toCourseLink(Course course) {
        return new CourseLinkResponse(course.getId(), course.getCourseCode(), course.getCourseTitle());
    }

    private BigDecimal total(List<CurriculumCourseResponse> courses, Function<CurriculumCourseResponse, BigDecimal> value) {
        return courses.stream()
                .map(value)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
