package com.school.sis.grade.entity;

import com.school.sis.common.audit.AuditableEntity;
import com.school.sis.curriculum.entity.Curriculum;
import com.school.sis.setup.entity.Course;
import com.school.sis.setup.entity.Faculty;
import com.school.sis.setup.entity.Program;
import com.school.sis.setup.entity.SchoolYear;
import com.school.sis.setup.entity.Semester;
import com.school.sis.student.entity.Student;
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
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "academic_records")
public class AcademicRecord extends AuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "grade_id", nullable = false, unique = true)
    private Grade grade;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curriculum_id", nullable = false)
    private Curriculum curriculum;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_year_id", nullable = false)
    private SchoolYear schoolYear;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @Column(name = "final_grade")
    private BigDecimal finalGrade;

    @Enumerated(EnumType.STRING)
    @Column(name = "special_grade")
    private SpecialGrade specialGrade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GradeRemark remark;

    @Enumerated(EnumType.STRING)
    @Column(name = "grade_status", nullable = false)
    private GradeStatus gradeStatus;

    @Column(name = "credit_units", nullable = false)
    private BigDecimal creditUnits = BigDecimal.ZERO;

    @Column(name = "earned_units", nullable = false)
    private BigDecimal earnedUnits = BigDecimal.ZERO;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "locked_at")
    private Instant lockedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    public UUID getId() { return id; }
    public Grade getGrade() { return grade; }
    public void setGrade(Grade grade) { this.grade = grade; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Program getProgram() { return program; }
    public void setProgram(Program program) { this.program = program; }
    public Curriculum getCurriculum() { return curriculum; }
    public void setCurriculum(Curriculum curriculum) { this.curriculum = curriculum; }
    public SchoolYear getSchoolYear() { return schoolYear; }
    public void setSchoolYear(SchoolYear schoolYear) { this.schoolYear = schoolYear; }
    public Semester getSemester() { return semester; }
    public void setSemester(Semester semester) { this.semester = semester; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public Faculty getFaculty() { return faculty; }
    public void setFaculty(Faculty faculty) { this.faculty = faculty; }
    public BigDecimal getFinalGrade() { return finalGrade; }
    public void setFinalGrade(BigDecimal finalGrade) { this.finalGrade = finalGrade; }
    public SpecialGrade getSpecialGrade() { return specialGrade; }
    public void setSpecialGrade(SpecialGrade specialGrade) { this.specialGrade = specialGrade; }
    public GradeRemark getRemark() { return remark; }
    public void setRemark(GradeRemark remark) { this.remark = remark; }
    public GradeStatus getGradeStatus() { return gradeStatus; }
    public void setGradeStatus(GradeStatus gradeStatus) { this.gradeStatus = gradeStatus; }
    public BigDecimal getCreditUnits() { return creditUnits; }
    public void setCreditUnits(BigDecimal creditUnits) { this.creditUnits = creditUnits; }
    public BigDecimal getEarnedUnits() { return earnedUnits; }
    public void setEarnedUnits(BigDecimal earnedUnits) { this.earnedUnits = earnedUnits; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public Instant getLockedAt() { return lockedAt; }
    public void setLockedAt(Instant lockedAt) { this.lockedAt = lockedAt; }
}
