package com.school.sis.schedule.repository;

import com.school.sis.schedule.entity.ScheduleMeeting;
import com.school.sis.schedule.entity.ScheduleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

public interface ScheduleMeetingRepository extends JpaRepository<ScheduleMeeting, UUID> {

    @Query("""
            select meeting
            from ScheduleMeeting meeting
            join fetch meeting.classSchedule schedule
            join fetch schedule.section section
            join fetch schedule.course course
            join fetch schedule.faculty faculty
            join fetch schedule.room room
            where schedule.schoolYear.id = :schoolYearId
              and schedule.semester.id = :semesterId
              and schedule.status = :status
              and meeting.dayOfWeek = :dayOfWeek
              and meeting.startTime < :endTime
              and meeting.endTime > :startTime
              and (:ignoreScheduleId is null or schedule.id <> :ignoreScheduleId)
              and (
                    schedule.room.id = :roomId
                 or schedule.faculty.id = :facultyId
                 or schedule.section.id = :sectionId
              )
            """)
    List<ScheduleMeeting> findOverlappingActiveMeetings(
            @Param("schoolYearId") UUID schoolYearId,
            @Param("semesterId") UUID semesterId,
            @Param("sectionId") UUID sectionId,
            @Param("facultyId") UUID facultyId,
            @Param("roomId") UUID roomId,
            @Param("dayOfWeek") DayOfWeek dayOfWeek,
            @Param("startTime") LocalTime startTime,
            @Param("endTime") LocalTime endTime,
            @Param("ignoreScheduleId") UUID ignoreScheduleId,
            @Param("status") ScheduleStatus status
    );
}
