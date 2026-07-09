package com.school.sis.curriculum.entity;

import com.school.sis.common.audit.AuditableEntity;
import com.school.sis.setup.entity.Course;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "curriculum_courses")
public class CurriculumCourse extends AuditableEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curriculum_id", nullable = false)
    private Curriculum curriculum;

    @Column(name = "year_level", nullable = false)
    private int yearLevel;

    @Column(nullable = false, length = 40)
    private String semester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder;

    @Enumerated(EnumType.STRING)
    @Column(name = "required_status", nullable = false)
    private RequiredStatus requiredStatus = RequiredStatus.REQUIRED;

    @ManyToMany
    @JoinTable(
            name = "curriculum_course_prerequisites",
            joinColumns = @JoinColumn(name = "curriculum_course_id"),
            inverseJoinColumns = @JoinColumn(name = "prerequisite_course_id")
    )
    private Set<Course> prerequisites = new LinkedHashSet<>();

    @ManyToMany
    @JoinTable(
            name = "curriculum_course_corequisites",
            joinColumns = @JoinColumn(name = "curriculum_course_id"),
            inverseJoinColumns = @JoinColumn(name = "corequisite_course_id")
    )
    private Set<Course> corequisites = new LinkedHashSet<>();

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public UUID getId() { return id; }
    public Curriculum getCurriculum() { return curriculum; }
    public void setCurriculum(Curriculum curriculum) { this.curriculum = curriculum; }
    public int getYearLevel() { return yearLevel; }
    public void setYearLevel(int yearLevel) { this.yearLevel = yearLevel; }
    public String getSemester() { return semester; }
    public void setSemester(String semester) { this.semester = semester; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public int getSortOrder() { return sortOrder; }
    public void setSortOrder(int sortOrder) { this.sortOrder = sortOrder; }
    public RequiredStatus getRequiredStatus() { return requiredStatus; }
    public void setRequiredStatus(RequiredStatus requiredStatus) { this.requiredStatus = requiredStatus; }
    public Set<Course> getPrerequisites() { return prerequisites; }
    public void setPrerequisites(Set<Course> prerequisites) { this.prerequisites = prerequisites; }
    public Set<Course> getCorequisites() { return corequisites; }
    public void setCorequisites(Set<Course> corequisites) { this.corequisites = corequisites; }
}
