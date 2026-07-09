package com.school.sis.auth.entity;

import com.school.sis.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "permissions")
public class Permission extends AuditableEntity {

    @Id
    private UUID id;

    @Column(nullable = false, unique = true, length = 120)
    private String name;

    @Column(nullable = false)
    private String description;

    protected Permission() {
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
