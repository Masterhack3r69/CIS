package com.school.sis.grade.entity;

import com.school.sis.auth.entity.User;
import com.school.sis.common.audit.AuditableEntity;
import com.school.sis.enrollment.entity.EnrollmentSubject;
import com.school.sis.schedule.entity.ClassSchedule;
import com.school.sis.setup.entity.Course;
import com.school.sis.setup.entity.Faculty;
import com.school.sis.setup.entity.SchoolYear;
import com.school.sis.setup.entity.Section;
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
@Table(name = "grades")
public class Grade extends AuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_subject_id", nullable = false, unique = true)
    private EnrollmentSubject enrollmentSubject;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_schedule_id", nullable = false)
    private ClassSchedule classSchedule;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "section_id", nullable = false)
    private Section section;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "faculty_id", nullable = false)
    private Faculty faculty;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_year_id", nullable = false)
    private SchoolYear schoolYear;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(name = "final_grade")
    private BigDecimal finalGrade;

    @Enumerated(EnumType.STRING)
    @Column(name = "special_grade")
    private SpecialGrade specialGrade;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GradeRemark remark;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GradeStatus status = GradeStatus.ENCODED;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encoded_by")
    private User encodedBy;

    @Column(name = "encoded_at")
    private Instant encodedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by")
    private User submittedBy;

    @Column(name = "submitted_at")
    private Instant submittedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by")
    private User reviewedBy;

    @Column(name = "reviewed_at")
    private Instant reviewedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;

    @Column(name = "approved_at")
    private Instant approvedAt;

    @Column(name = "locked_at")
    private Instant lockedAt;

    private String notes;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    public UUID getId() { return id; }
    public EnrollmentSubject getEnrollmentSubject() { return enrollmentSubject; }
    public void setEnrollmentSubject(EnrollmentSubject enrollmentSubject) { this.enrollmentSubject = enrollmentSubject; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public ClassSchedule getClassSchedule() { return classSchedule; }
    public void setClassSchedule(ClassSchedule classSchedule) { this.classSchedule = classSchedule; }
    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
    public Section getSection() { return section; }
    public void setSection(Section section) { this.section = section; }
    public Faculty getFaculty() { return faculty; }
    public void setFaculty(Faculty faculty) { this.faculty = faculty; }
    public SchoolYear getSchoolYear() { return schoolYear; }
    public void setSchoolYear(SchoolYear schoolYear) { this.schoolYear = schoolYear; }
    public Semester getSemester() { return semester; }
    public void setSemester(Semester semester) { this.semester = semester; }
    public BigDecimal getFinalGrade() { return finalGrade; }
    public void setFinalGrade(BigDecimal finalGrade) { this.finalGrade = finalGrade; }
    public SpecialGrade getSpecialGrade() { return specialGrade; }
    public void setSpecialGrade(SpecialGrade specialGrade) { this.specialGrade = specialGrade; }
    public GradeRemark getRemark() { return remark; }
    public void setRemark(GradeRemark remark) { this.remark = remark; }
    public GradeStatus getStatus() { return status; }
    public void setStatus(GradeStatus status) { this.status = status; }
    public User getEncodedBy() { return encodedBy; }
    public void setEncodedBy(User encodedBy) { this.encodedBy = encodedBy; }
    public Instant getEncodedAt() { return encodedAt; }
    public void setEncodedAt(Instant encodedAt) { this.encodedAt = encodedAt; }
    public User getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(User submittedBy) { this.submittedBy = submittedBy; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
    public User getReviewedBy() { return reviewedBy; }
    public void setReviewedBy(User reviewedBy) { this.reviewedBy = reviewedBy; }
    public Instant getReviewedAt() { return reviewedAt; }
    public void setReviewedAt(Instant reviewedAt) { this.reviewedAt = reviewedAt; }
    public User getApprovedBy() { return approvedBy; }
    public void setApprovedBy(User approvedBy) { this.approvedBy = approvedBy; }
    public Instant getApprovedAt() { return approvedAt; }
    public void setApprovedAt(Instant approvedAt) { this.approvedAt = approvedAt; }
    public Instant getLockedAt() { return lockedAt; }
    public void setLockedAt(Instant lockedAt) { this.lockedAt = lockedAt; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}
