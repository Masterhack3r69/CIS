package com.school.sis.fee.entity;

import com.school.sis.common.audit.AuditableEntity;
import com.school.sis.setup.entity.ActiveStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "fee_items")
public class FeeItem extends AuditableEntity {
    @Id
    private UUID id;

    @Column(name = "fee_code", nullable = false, unique = true, length = 60)
    private String feeCode;

    @Column(name = "fee_name", nullable = false)
    private String feeName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeeCategory category;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActiveStatus status = ActiveStatus.ACTIVE;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    public UUID getId() { return id; }
    public String getFeeCode() { return feeCode; }
    public void setFeeCode(String feeCode) { this.feeCode = feeCode; }
    public String getFeeName() { return feeName; }
    public void setFeeName(String feeName) { this.feeName = feeName; }
    public FeeCategory getCategory() { return category; }
    public void setCategory(FeeCategory category) { this.category = category; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ActiveStatus getStatus() { return status; }
    public void setStatus(ActiveStatus status) { this.status = status; }
}
