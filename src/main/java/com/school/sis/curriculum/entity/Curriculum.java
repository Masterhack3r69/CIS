package com.school.sis.curriculum.entity;

import com.school.sis.common.audit.AuditableEntity;
import com.school.sis.setup.entity.Program;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "curricula")
public class Curriculum extends AuditableEntity {

    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @Column(name = "curriculum_code", nullable = false, unique = true, length = 60)
    private String curriculumCode;

    @Column(name = "curriculum_name", nullable = false)
    private String curriculumName;

    @Column(name = "effective_school_year", nullable = false, length = 20)
    private String effectiveSchoolYear;

    @Column(nullable = false, length = 40)
    private String version;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CurriculumStatus status = CurriculumStatus.DRAFT;

    private String description;

    @OneToMany(mappedBy = "curriculum", orphanRemoval = true)
    private List<CurriculumCourse> courses = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (id == null) {
            id = UUID.randomUUID();
        }
    }

    public UUID getId() { return id; }
    public Program getProgram() { return program; }
    public void setProgram(Program program) { this.program = program; }
    public String getCurriculumCode() { return curriculumCode; }
    public void setCurriculumCode(String curriculumCode) { this.curriculumCode = curriculumCode; }
    public String getCurriculumName() { return curriculumName; }
    public void setCurriculumName(String curriculumName) { this.curriculumName = curriculumName; }
    public String getEffectiveSchoolYear() { return effectiveSchoolYear; }
    public void setEffectiveSchoolYear(String effectiveSchoolYear) { this.effectiveSchoolYear = effectiveSchoolYear; }
    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }
    public CurriculumStatus getStatus() { return status; }
    public void setStatus(CurriculumStatus status) { this.status = status; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public List<CurriculumCourse> getCourses() { return courses; }
}
