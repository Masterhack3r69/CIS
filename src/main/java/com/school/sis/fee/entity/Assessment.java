package com.school.sis.fee.entity;

import com.school.sis.common.audit.AuditableEntity;
import com.school.sis.enrollment.entity.Enrollment;
import com.school.sis.setup.entity.SchoolYear;
import com.school.sis.setup.entity.Semester;
import com.school.sis.student.entity.Student;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "assessments")
public class Assessment extends AuditableEntity {
    @Id
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "enrollment_id", nullable = false)
    private Enrollment enrollment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "school_year_id", nullable = false)
    private SchoolYear schoolYear;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "semester_id", nullable = false)
    private Semester semester;

    @Column(name = "total_units", nullable = false)
    private BigDecimal totalUnits = BigDecimal.ZERO;

    @Column(name = "tuition_amount", nullable = false)
    private BigDecimal tuitionAmount = BigDecimal.ZERO;

    @Column(name = "laboratory_fee_amount", nullable = false)
    private BigDecimal laboratoryFeeAmount = BigDecimal.ZERO;

    @Column(name = "miscellaneous_fee_amount", nullable = false)
    private BigDecimal miscellaneousFeeAmount = BigDecimal.ZERO;

    @Column(name = "other_fee_amount", nullable = false)
    private BigDecimal otherFeeAmount = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "penalty_amount", nullable = false)
    private BigDecimal penaltyAmount = BigDecimal.ZERO;

    @Column(name = "total_assessment", nullable = false)
    private BigDecimal totalAssessment = BigDecimal.ZERO;

    @Column(name = "amount_paid", nullable = false)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AssessmentStatus status = AssessmentStatus.UNPAID;

    @OneToMany(mappedBy = "assessment", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AssessmentItem> items = new ArrayList<>();

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    public UUID getId() { return id; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public Enrollment getEnrollment() { return enrollment; }
    public void setEnrollment(Enrollment enrollment) { this.enrollment = enrollment; }
    public SchoolYear getSchoolYear() { return schoolYear; }
    public void setSchoolYear(SchoolYear schoolYear) { this.schoolYear = schoolYear; }
    public Semester getSemester() { return semester; }
    public void setSemester(Semester semester) { this.semester = semester; }
    public BigDecimal getTotalUnits() { return totalUnits; }
    public void setTotalUnits(BigDecimal totalUnits) { this.totalUnits = totalUnits; }
    public BigDecimal getTuitionAmount() { return tuitionAmount; }
    public void setTuitionAmount(BigDecimal tuitionAmount) { this.tuitionAmount = tuitionAmount; }
    public BigDecimal getLaboratoryFeeAmount() { return laboratoryFeeAmount; }
    public void setLaboratoryFeeAmount(BigDecimal laboratoryFeeAmount) { this.laboratoryFeeAmount = laboratoryFeeAmount; }
    public BigDecimal getMiscellaneousFeeAmount() { return miscellaneousFeeAmount; }
    public void setMiscellaneousFeeAmount(BigDecimal miscellaneousFeeAmount) { this.miscellaneousFeeAmount = miscellaneousFeeAmount; }
    public BigDecimal getOtherFeeAmount() { return otherFeeAmount; }
    public void setOtherFeeAmount(BigDecimal otherFeeAmount) { this.otherFeeAmount = otherFeeAmount; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public void setDiscountAmount(BigDecimal discountAmount) { this.discountAmount = discountAmount; }
    public BigDecimal getPenaltyAmount() { return penaltyAmount; }
    public void setPenaltyAmount(BigDecimal penaltyAmount) { this.penaltyAmount = penaltyAmount; }
    public BigDecimal getTotalAssessment() { return totalAssessment; }
    public void setTotalAssessment(BigDecimal totalAssessment) { this.totalAssessment = totalAssessment; }
    public BigDecimal getAmountPaid() { return amountPaid; }
    public void setAmountPaid(BigDecimal amountPaid) { this.amountPaid = amountPaid; }
    public BigDecimal getBalance() { return balance; }
    public void setBalance(BigDecimal balance) { this.balance = balance; }
    public AssessmentStatus getStatus() { return status; }
    public void setStatus(AssessmentStatus status) { this.status = status; }
    public List<AssessmentItem> getItems() { return items; }
    public void setItems(List<AssessmentItem> items) {
        this.items.clear();
        if (items != null) {
            items.forEach(this::addItem);
        }
    }
    public void addItem(AssessmentItem item) {
        item.setAssessment(this);
        items.add(item);
    }
}
