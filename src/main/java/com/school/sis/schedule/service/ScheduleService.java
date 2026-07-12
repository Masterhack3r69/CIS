package com.school.sis.schedule.service;

import com.school.sis.audit.AuditModule;
import com.school.sis.audit.service.AuditService;
import com.school.sis.common.exception.BusinessRuleException;
import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.response.PageResponse;
import com.school.sis.schedule.dto.ScheduleConflictDetail;
import com.school.sis.schedule.dto.ScheduleConflictRequest;
import com.school.sis.schedule.dto.ScheduleConflictResponse;
import com.school.sis.schedule.dto.ScheduleMeetingRequest;
import com.school.sis.schedule.dto.ScheduleMeetingResponse;
import com.school.sis.schedule.dto.ScheduleRequest;
import com.school.sis.schedule.dto.ScheduleResponse;
import com.school.sis.schedule.dto.ScheduleSearchCriteria;
import com.school.sis.schedule.entity.ClassSchedule;
import com.school.sis.schedule.entity.ScheduleMeeting;
import com.school.sis.schedule.entity.ScheduleStatus;
import com.school.sis.schedule.repository.ClassScheduleRepository;
import com.school.sis.schedule.repository.ScheduleMeetingRepository;
import com.school.sis.enrollment.repository.EnrollmentSubjectRepository;
import com.school.sis.enrollment.entity.EnrollmentStatus;
import com.school.sis.enrollment.entity.EnrollmentSubjectStatus;
import com.school.sis.curriculum.repository.CurriculumCourseRepository;
import com.school.sis.setup.entity.ActiveStatus;
import com.school.sis.setup.entity.Course;
import com.school.sis.setup.entity.Faculty;
import com.school.sis.setup.entity.Room;
import com.school.sis.setup.entity.SchoolYear;
import com.school.sis.setup.entity.Section;
import com.school.sis.setup.entity.Semester;
import com.school.sis.setup.repository.CourseRepository;
import com.school.sis.setup.repository.FacultyRepository;
import com.school.sis.setup.repository.RoomRepository;
import com.school.sis.setup.repository.SectionRepository;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class ScheduleService {

    private static final String ROOM_CONFLICT = "ROOM";
    private static final String FACULTY_CONFLICT = "FACULTY";
    private static final String SECTION_CONFLICT = "SECTION";

    private final ClassScheduleRepository classScheduleRepository;
    private final ScheduleMeetingRepository scheduleMeetingRepository;
    private final SectionRepository sectionRepository;
    private final CourseRepository courseRepository;
    private final FacultyRepository facultyRepository;
    private final RoomRepository roomRepository;
    private final CurriculumCourseRepository curriculumCourseRepository;
    private final AuditService auditService;
    private final EnrollmentSubjectRepository enrollmentSubjectRepository;

    public ScheduleService(
            ClassScheduleRepository classScheduleRepository,
            ScheduleMeetingRepository scheduleMeetingRepository,
            SectionRepository sectionRepository,
            CourseRepository courseRepository,
            FacultyRepository facultyRepository,
            RoomRepository roomRepository,
            CurriculumCourseRepository curriculumCourseRepository,
            AuditService auditService,
            EnrollmentSubjectRepository enrollmentSubjectRepository
    ) {
        this.classScheduleRepository = classScheduleRepository;
        this.scheduleMeetingRepository = scheduleMeetingRepository;
        this.sectionRepository = sectionRepository;
        this.courseRepository = courseRepository;
        this.facultyRepository = facultyRepository;
        this.roomRepository = roomRepository;
        this.curriculumCourseRepository = curriculumCourseRepository;
        this.auditService = auditService;
        this.enrollmentSubjectRepository = enrollmentSubjectRepository;
    }

    @Transactional(readOnly = true)
    public PageResponse<ScheduleResponse> list(ScheduleSearchCriteria criteria, Pageable pageable) {
        return PageResponse.from(classScheduleRepository.findAll(specification(criteria), pageable).map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public ScheduleResponse get(UUID id) {
        return toResponse(findSchedule(id));
    }

    @Transactional
    public ScheduleResponse create(ScheduleRequest request) {
        validateMeetings(request.meetings());
        validateNoConflictsForActiveSchedule(null, request);
        ClassSchedule schedule = new ClassSchedule();
        apply(schedule, request);
        ScheduleResponse response = toResponse(classScheduleRepository.save(schedule));
        auditService.log("SCHEDULE_CREATED", AuditModule.SCHEDULE, "ClassSchedule", response.id(), null, response);
        return response;
    }

    @Transactional
    public ScheduleResponse update(UUID id, ScheduleRequest request) {
        validateMeetings(request.meetings());
        validateNoConflictsForActiveSchedule(id, request);
        ClassSchedule schedule = findSchedule(id);
        ScheduleResponse before = toResponse(schedule);
        apply(schedule, request);
        ScheduleResponse after = toResponse(schedule);
        auditService.log("SCHEDULE_UPDATED", AuditModule.SCHEDULE, "ClassSchedule", id, before, after);
        return after;
    }

    @Transactional
    public void delete(UUID id) {
        ClassSchedule schedule = findSchedule(id);
        ScheduleStatus before = schedule.getStatus();
        schedule.setStatus(ScheduleStatus.ARCHIVED);
        auditService.log("SCHEDULE_ARCHIVED", AuditModule.SCHEDULE, "ClassSchedule", id,
                java.util.Map.of("status", before), java.util.Map.of("status", ScheduleStatus.ARCHIVED));
    }

    @Transactional(readOnly = true)
    public ScheduleConflictResponse checkConflict(ScheduleConflictRequest request) {
        validateMeetings(request.meetings());
        List<ScheduleConflictDetail> conflicts = findConflicts(
                request.ignoreScheduleId(),
                request.sectionId(),
                request.facultyId(),
                request.roomId(),
                request.schoolYearId(),
                request.semesterId(),
                request.meetings()
        );
        return new ScheduleConflictResponse(!conflicts.isEmpty(), conflicts);
    }

    private void apply(ClassSchedule schedule, ScheduleRequest request) {
        Section section = findSection(request.sectionId());
        if (section.getCurriculum() == null) throw new BusinessRuleException("Section must have a curriculum");
        Course course = courseRepository.findById(request.courseId())
                .orElseThrow(() -> new NotFoundException("Course not found"));
        Faculty faculty = facultyRepository.findById(request.facultyId())
                .orElseThrow(() -> new NotFoundException("Faculty not found"));
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new NotFoundException("Room not found"));
        String semesterCode = section.getSemester().getName().trim().toUpperCase(Locale.ROOT).replaceAll("[^A-Z0-9]+", "_");
        if (!curriculumCourseRepository.existsByCurriculumIdAndYearLevelAndSemesterIgnoreCaseAndCourseId(
                section.getCurriculum().getId(), section.getYearLevel(), semesterCode, course.getId())) {
            throw new BusinessRuleException("Course is not part of the section curriculum term");
        }
        validateAssignable(faculty, room, section, request.status());

        schedule.setSection(section);
        schedule.setCourse(course);
        schedule.setFaculty(faculty);
        schedule.setRoom(room);
        schedule.setSchoolYear(section.getSchoolYear());
        schedule.setSemester(section.getSemester());
        schedule.setCapacity(request.capacity());
        schedule.setStatus(request.status());
        schedule.setMeetings(request.meetings().stream().map(this::toMeeting).toList());
    }

    private void validateNoConflictsForActiveSchedule(UUID currentScheduleId, ScheduleRequest request) {
        if (request.status() != ScheduleStatus.ACTIVE) {
            return;
        }
        Section section = findSection(request.sectionId());
        List<ScheduleConflictDetail> conflicts = findConflicts(
                currentScheduleId,
                request.sectionId(),
                request.facultyId(),
                request.roomId(),
                section.getSchoolYear().getId(),
                section.getSemester().getId(),
                request.meetings()
        );
        if (!conflicts.isEmpty()) {
            throw new BusinessRuleException("Schedule has conflicts");
        }
    }

    private List<ScheduleConflictDetail> findConflicts(
            UUID ignoreScheduleId,
            UUID sectionId,
            UUID facultyId,
            UUID roomId,
            UUID schoolYearId,
            UUID semesterId,
            List<ScheduleMeetingRequest> meetings
    ) {
        List<ScheduleConflictDetail> conflicts = new ArrayList<>();
        for (ScheduleMeetingRequest requested : meetings) {
            List<ScheduleMeeting> overlaps = scheduleMeetingRepository.findOverlappingActiveMeetings(
                    schoolYearId,
                    semesterId,
                    sectionId,
                    facultyId,
                    roomId,
                    requested.dayOfWeek(),
                    requested.startTime(),
                    requested.endTime(),
                    ignoreScheduleId,
                    ScheduleStatus.ACTIVE
            );
            for (ScheduleMeeting overlap : overlaps) {
                ClassSchedule schedule = overlap.getClassSchedule();
                if (schedule.getRoom().getId().equals(roomId)) {
                    conflicts.add(toConflictDetail(ROOM_CONFLICT, schedule, overlap, requested));
                }
                if (schedule.getFaculty().getId().equals(facultyId)) {
                    conflicts.add(toConflictDetail(FACULTY_CONFLICT, schedule, overlap, requested));
                }
                if (schedule.getSection().getId().equals(sectionId)) {
                    conflicts.add(toConflictDetail(SECTION_CONFLICT, schedule, overlap, requested));
                }
            }
        }
        return conflicts;
    }

    private void validateMeetings(List<ScheduleMeetingRequest> meetings) {
        if (meetings == null || meetings.isEmpty()) {
            throw new BusinessRuleException("At least one schedule meeting is required");
        }
        for (ScheduleMeetingRequest meeting : meetings) {
            if (meeting.startTime() == null || meeting.endTime() == null || meeting.dayOfWeek() == null) {
                throw new BusinessRuleException("Meeting day, start time, and end time are required");
            }
            if (!meeting.endTime().isAfter(meeting.startTime())) {
                throw new BusinessRuleException("Meeting end time must be after start time");
            }
        }
    }

    private void validateTermMatchesSection(Section section, SchoolYear schoolYear, Semester semester) {
        if (!section.getSchoolYear().getId().equals(schoolYear.getId()) || !section.getSemester().getId().equals(semester.getId())) {
            throw new BusinessRuleException("Schedule term must match the selected section term");
        }
    }

    private void validateAssignable(Faculty faculty, Room room, Section section, ScheduleStatus status) {
        if (status != ScheduleStatus.ACTIVE) {
            return;
        }
        if (faculty.getStatus() != ActiveStatus.ACTIVE) {
            throw new BusinessRuleException("Inactive faculty cannot be assigned to an active schedule");
        }
        if (room.getStatus() != ActiveStatus.ACTIVE) {
            throw new BusinessRuleException("Inactive room cannot be assigned to an active schedule");
        }
        if (section.getStatus() != ActiveStatus.ACTIVE) {
            throw new BusinessRuleException("Inactive section cannot be assigned to an active schedule");
        }
    }

    private Section findSection(UUID id) {
        return sectionRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Section not found"));
    }

    private ClassSchedule findSchedule(UUID id) {
        return classScheduleRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Schedule not found"));
    }

    private ScheduleMeeting toMeeting(ScheduleMeetingRequest request) {
        ScheduleMeeting meeting = new ScheduleMeeting();
        meeting.setDayOfWeek(request.dayOfWeek());
        meeting.setStartTime(request.startTime());
        meeting.setEndTime(request.endTime());
        return meeting;
    }

    private ScheduleResponse toResponse(ClassSchedule schedule) {
        long enrolledCount = enrollmentSubjectRepository.countByClassScheduleIdAndStatusAndEnrollmentStatus(
                schedule.getId(), EnrollmentSubjectStatus.ENROLLED, EnrollmentStatus.CONFIRMED);
        return new ScheduleResponse(
                schedule.getId(),
                schedule.getSection().getId(),
                schedule.getSection().getSectionCode(),
                schedule.getSection().getProgram().getId(),
                schedule.getSection().getProgram().getProgramCode(),
                schedule.getSection().getCurriculum() == null ? null : schedule.getSection().getCurriculum().getId(),
                schedule.getSection().getCurriculum() == null ? null : schedule.getSection().getCurriculum().getCurriculumCode(),
                schedule.getSection().getYearLevel(),
                schedule.getCourse().getId(),
                schedule.getCourse().getCourseCode(),
                schedule.getCourse().getCourseTitle(),
                schedule.getCourse().getCreditUnits(),
                schedule.getFaculty().getId(),
                facultyName(schedule.getFaculty()),
                schedule.getRoom().getId(),
                schedule.getRoom().getRoomCode(),
                schedule.getSchoolYear().getId(),
                schedule.getSchoolYear().getSchoolYear(),
                schedule.getSemester().getId(),
                schedule.getSemester().getName(),
                schedule.getCapacity(),
                enrolledCount,
                Math.max(0, schedule.getCapacity() - enrolledCount),
                schedule.getStatus(),
                schedule.getMeetings().stream()
                        .sorted(Comparator.comparing(ScheduleMeeting::getDayOfWeek).thenComparing(ScheduleMeeting::getStartTime))
                        .map(this::toMeetingResponse)
                        .toList()
        );
    }

    private ScheduleMeetingResponse toMeetingResponse(ScheduleMeeting meeting) {
        return new ScheduleMeetingResponse(meeting.getId(), meeting.getDayOfWeek(), meeting.getStartTime(), meeting.getEndTime());
    }

    private ScheduleConflictDetail toConflictDetail(
            String conflictType,
            ClassSchedule schedule,
            ScheduleMeeting existingMeeting,
            ScheduleMeetingRequest requestedMeeting
    ) {
        return new ScheduleConflictDetail(
                conflictType,
                schedule.getId(),
                schedule.getCourse().getCourseCode(),
                schedule.getCourse().getCourseTitle(),
                schedule.getSection().getSectionCode(),
                facultyName(schedule.getFaculty()),
                schedule.getRoom().getRoomCode(),
                existingMeeting.getDayOfWeek(),
                existingMeeting.getStartTime(),
                existingMeeting.getEndTime(),
                requestedMeeting.startTime(),
                requestedMeeting.endTime()
        );
    }

    private Specification<ClassSchedule> specification(ScheduleSearchCriteria criteria) {
        return (root, query, cb) -> {
            query.distinct(true);
            if (criteria == null) {
                return cb.conjunction();
            }
            var predicate = cb.conjunction();
            if (criteria.search() != null && !criteria.search().isBlank()) {
                String term = "%" + criteria.search().toLowerCase(Locale.ROOT) + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("section").get("sectionCode")), term),
                        cb.like(cb.lower(root.get("course").get("courseCode")), term),
                        cb.like(cb.lower(root.get("course").get("courseTitle")), term),
                        cb.like(cb.lower(root.get("faculty").get("firstName")), term),
                        cb.like(cb.lower(root.get("faculty").get("lastName")), term),
                        cb.like(cb.lower(root.get("room").get("roomCode")), term)
                ));
            }
            if (criteria.schoolYearId() != null) predicate = cb.and(predicate, cb.equal(root.get("schoolYear").get("id"), criteria.schoolYearId()));
            if (criteria.semesterId() != null) predicate = cb.and(predicate, cb.equal(root.get("semester").get("id"), criteria.semesterId()));
            if (criteria.programId() != null) predicate = cb.and(predicate, cb.equal(root.get("section").get("program").get("id"), criteria.programId()));
            if (criteria.sectionId() != null) predicate = cb.and(predicate, cb.equal(root.get("section").get("id"), criteria.sectionId()));
            if (criteria.facultyId() != null) predicate = cb.and(predicate, cb.equal(root.get("faculty").get("id"), criteria.facultyId()));
            if (criteria.roomId() != null) predicate = cb.and(predicate, cb.equal(root.get("room").get("id"), criteria.roomId()));
            if (criteria.courseId() != null) predicate = cb.and(predicate, cb.equal(root.get("course").get("id"), criteria.courseId()));
            if (criteria.status() != null) predicate = cb.and(predicate, cb.equal(root.get("status"), criteria.status()));
            if (criteria.curriculumId() != null) predicate = cb.and(predicate, cb.equal(root.get("section").get("curriculum").get("id"), criteria.curriculumId()));
            if (criteria.yearLevel() != null) predicate = cb.and(predicate, cb.equal(root.get("section").get("yearLevel"), criteria.yearLevel()));
            if (criteria.dayOfWeek() != null) {
                var meeting = root.join("meetings", JoinType.INNER);
                predicate = cb.and(predicate, cb.equal(meeting.get("dayOfWeek"), criteria.dayOfWeek()));
            }
            return predicate;
        };
    }

    private String facultyName(Faculty faculty) {
        if (faculty == null) return null;
        String middleInitial = faculty.getMiddleName() != null && !faculty.getMiddleName().isBlank() ? faculty.getMiddleName().substring(0, 1).toUpperCase() + "." : "";
        return String.join(" ", List.of(faculty.getFirstName(), middleInitial, faculty.getLastName(), blankToEmpty(faculty.getSuffix())))
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value;
    }
}
