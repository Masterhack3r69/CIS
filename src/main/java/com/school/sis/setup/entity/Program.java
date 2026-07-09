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
@Table(name = "programs")
public class Program extends AuditableEntity {

    @Id
    private UUID id;

    @Column(name = "program_code", nullable = false, unique = true, length = 40)
    private String programCode;

    @Column(name = "program_name", nullable = false)
    private String programName;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(name = "degree_type", nullable = false)
    private DegreeType degreeType;

    @Column(name = "program_duration")
    private Integer programDuration;

    private String description;

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
    public String getProgramCode() { return programCode; }
    public void setProgramCode(String programCode) { this.programCode = programCode; }
    public String getProgramName() { return programName; }
    public void setProgramName(String programName) { this.programName = programName; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public DegreeType getDegreeType() { return degreeType; }
    public void setDegreeType(DegreeType degreeType) { this.degreeType = degreeType; }
    public Integer getProgramDuration() { return programDuration; }
    public void setProgramDuration(Integer programDuration) { this.programDuration = programDuration; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ActiveStatus getStatus() { return status; }
    public void setStatus(ActiveStatus status) { this.status = status; }
}
