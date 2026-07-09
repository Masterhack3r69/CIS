package com.school.sis.setup.entity;

import com.school.sis.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "courses")
public class Course extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "course_code", nullable = false, unique = true, length = 40)
    private String courseCode;

    @Column(name = "course_title", nullable = false)
    private String courseTitle;

    @Column(name = "course_description")
    private String courseDescription;

    @Column(name = "lecture_hours_per_week", nullable = false)
    private BigDecimal lectureHoursPerWeek = BigDecimal.ZERO;

    @Column(name = "laboratory_hours_per_week", nullable = false)
    private BigDecimal laboratoryHoursPerWeek = BigDecimal.ZERO;

    @Column(name = "credit_units", nullable = false)
    private BigDecimal creditUnits = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "course_type", nullable = false)
    private CourseType courseType;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActiveStatus status = ActiveStatus.ACTIVE;

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public UUID getId() { return id; }
    public String getCourseCode() { return courseCode; }
    public void setCourseCode(String courseCode) { this.courseCode = courseCode; }
    public String getCourseTitle() { return courseTitle; }
    public void setCourseTitle(String courseTitle) { this.courseTitle = courseTitle; }
    public String getCourseDescription() { return courseDescription; }
    public void setCourseDescription(String courseDescription) { this.courseDescription = courseDescription; }
    public BigDecimal getLectureHoursPerWeek() { return lectureHoursPerWeek; }
    public void setLectureHoursPerWeek(BigDecimal lectureHoursPerWeek) { this.lectureHoursPerWeek = lectureHoursPerWeek; }
    public BigDecimal getLaboratoryHoursPerWeek() { return laboratoryHoursPerWeek; }
    public void setLaboratoryHoursPerWeek(BigDecimal laboratoryHoursPerWeek) { this.laboratoryHoursPerWeek = laboratoryHoursPerWeek; }
    public BigDecimal getCreditUnits() { return creditUnits; }
    public void setCreditUnits(BigDecimal creditUnits) { this.creditUnits = creditUnits; }
    public CourseType getCourseType() { return courseType; }
    public void setCourseType(CourseType courseType) { this.courseType = courseType; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public ActiveStatus getStatus() { return status; }
    public void setStatus(ActiveStatus status) { this.status = status; }
}
