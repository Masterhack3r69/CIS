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

import java.util.UUID;

@Entity
@Table(name = "sections")
public class Section extends AuditableEntity {
    @Id
    private UUID id;

    @Column(name = "section_code", nullable = false, length = 60)
    private String sectionCode;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_year_id", nullable = false)
    private SchoolYear schoolYear;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(name = "year_level", nullable = false)
    private int yearLevel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActiveStatus status = ActiveStatus.ACTIVE;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    public UUID getId() { return id; }
    public String getSectionCode() { return sectionCode; }
    public void setSectionCode(String sectionCode) { this.sectionCode = sectionCode; }
    public Program getProgram() { return program; }
    public void setProgram(Program program) { this.program = program; }
    public SchoolYear getSchoolYear() { return schoolYear; }
    public void setSchoolYear(SchoolYear schoolYear) { this.schoolYear = schoolYear; }
    public Semester getSemester() { return semester; }
    public void setSemester(Semester semester) { this.semester = semester; }
    public int getYearLevel() { return yearLevel; }
    public void setYearLevel(int yearLevel) { this.yearLevel = yearLevel; }
    public ActiveStatus getStatus() { return status; }
    public void setStatus(ActiveStatus status) { this.status = status; }
}
