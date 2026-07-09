package com.school.sis.enrollment.entity;

import com.school.sis.common.audit.AuditableEntity;
import com.school.sis.schedule.entity.ClassSchedule;
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

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "enrollment_subjects")
public class EnrollmentSubject extends AuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "class_schedule_id", nullable = false)
    private ClassSchedule classSchedule;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentSubjectStatus status = EnrollmentSubjectStatus.ENROLLED;

    @Column(name = "dropped_at")
    private Instant droppedAt;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    public UUID getId() { return id; }
    public Enrollment getEnrollment() { return enrollment; }
    public void setEnrollment(Enrollment enrollment) { this.enrollment = enrollment; }
    public ClassSchedule getClassSchedule() { return classSchedule; }
    public void setClassSchedule(ClassSchedule classSchedule) { this.classSchedule = classSchedule; }
    public EnrollmentSubjectStatus getStatus() { return status; }
    public void setStatus(EnrollmentSubjectStatus status) { this.status = status; }
    public Instant getDroppedAt() { return droppedAt; }
    public void setDroppedAt(Instant droppedAt) { this.droppedAt = droppedAt; }
}
