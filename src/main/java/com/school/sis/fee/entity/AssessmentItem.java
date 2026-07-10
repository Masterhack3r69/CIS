package com.school.sis.fee.entity;

import com.school.sis.common.audit.AuditableEntity;
import com.school.sis.enrollment.entity.EnrollmentSubject;
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
import java.util.UUID;

@Entity
@Table(name = "assessment_items")
public class AssessmentItem extends AuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assessment_id", nullable = false)
    private Assessment assessment;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fee_item_id")
    private FeeItem feeItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "enrollment_subject_id")
    private EnrollmentSubject enrollmentSubject;

    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FeeCategory category;

    @Enumerated(EnumType.STRING)
    @Column(name = "computation_type", nullable = false)
    private FeeComputationType computationType;

    @Column(nullable = false)
    private BigDecimal quantity = BigDecimal.ZERO;

    @Column(name = "unit_amount", nullable = false)
    private BigDecimal unitAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    public UUID getId() { return id; }
    public Assessment getAssessment() { return assessment; }
    public void setAssessment(Assessment assessment) { this.assessment = assessment; }
    public FeeItem getFeeItem() { return feeItem; }
    public void setFeeItem(FeeItem feeItem) { this.feeItem = feeItem; }
    public EnrollmentSubject getEnrollmentSubject() { return enrollmentSubject; }
    public void setEnrollmentSubject(EnrollmentSubject enrollmentSubject) { this.enrollmentSubject = enrollmentSubject; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public FeeCategory getCategory() { return category; }
    public void setCategory(FeeCategory category) { this.category = category; }
    public FeeComputationType getComputationType() { return computationType; }
    public void setComputationType(FeeComputationType computationType) { this.computationType = computationType; }
    public BigDecimal getQuantity() { return quantity; }
    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }
    public BigDecimal getUnitAmount() { return unitAmount; }
    public void setUnitAmount(BigDecimal unitAmount) { this.unitAmount = unitAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
}
