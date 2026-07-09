package com.school.sis.setup.entity;

import com.school.sis.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "school_years")
public class SchoolYear extends AuditableEntity {
    @Id
    private UUID id;

    @Column(name = "school_year", nullable = false, unique = true, length = 20)
    private String schoolYear;

    @Column(nullable = false)
    private boolean active;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    public UUID getId() { return id; }
    public String getSchoolYear() { return schoolYear; }
    public void setSchoolYear(String schoolYear) { this.schoolYear = schoolYear; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
}
