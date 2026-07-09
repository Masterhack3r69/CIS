package com.school.sis.fee.entity;

import com.school.sis.common.audit.AuditableEntity;
import com.school.sis.setup.entity.ActiveStatus;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.util.ArrayList;
import java.util.List;
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

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActiveStatus status = ActiveStatus.ACTIVE;

    @OneToMany(mappedBy = "feeItem", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeeRule> rules = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    public UUID getId() { return id; }
    public String getFeeCode() { return feeCode; }
    public void setFeeCode(String feeCode) { this.feeCode = feeCode; }
    public String getFeeName() { return feeName; }
    public void setFeeName(String feeName) { this.feeName = feeName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public ActiveStatus getStatus() { return status; }
    public void setStatus(ActiveStatus status) { this.status = status; }
    public List<FeeRule> getRules() { return rules; }
    public void setRules(List<FeeRule> rules) {
        this.rules.clear();
        if (rules != null) {
            rules.forEach(this::addRule);
        }
    }
    public void addRule(FeeRule rule) {
        rule.setFeeItem(this);
        rules.add(rule);
    }
}
