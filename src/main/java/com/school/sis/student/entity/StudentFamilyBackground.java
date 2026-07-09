package com.school.sis.student.entity;

import com.school.sis.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "student_family_backgrounds")
public class StudentFamilyBackground extends AuditableEntity {
    @Id
    @Column(name = "student_id")
    private UUID studentId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "student_id")
    private Student student;

    private String fatherName;
    private String fatherOccupation;
    private String fatherContactNumber;
    private String motherName;
    private String motherOccupation;
    private String motherContactNumber;
    private String guardianName;
    private String guardianRelationship;
    private String guardianContactNumber;
    private String guardianAddress;
    private String householdIncomeRange;

    public UUID getStudentId() { return studentId; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public String getFatherName() { return fatherName; }
    public void setFatherName(String fatherName) { this.fatherName = fatherName; }
    public String getFatherOccupation() { return fatherOccupation; }
    public void setFatherOccupation(String fatherOccupation) { this.fatherOccupation = fatherOccupation; }
    public String getFatherContactNumber() { return fatherContactNumber; }
    public void setFatherContactNumber(String fatherContactNumber) { this.fatherContactNumber = fatherContactNumber; }
    public String getMotherName() { return motherName; }
    public void setMotherName(String motherName) { this.motherName = motherName; }
    public String getMotherOccupation() { return motherOccupation; }
    public void setMotherOccupation(String motherOccupation) { this.motherOccupation = motherOccupation; }
    public String getMotherContactNumber() { return motherContactNumber; }
    public void setMotherContactNumber(String motherContactNumber) { this.motherContactNumber = motherContactNumber; }
    public String getGuardianName() { return guardianName; }
    public void setGuardianName(String guardianName) { this.guardianName = guardianName; }
    public String getGuardianRelationship() { return guardianRelationship; }
    public void setGuardianRelationship(String guardianRelationship) { this.guardianRelationship = guardianRelationship; }
    public String getGuardianContactNumber() { return guardianContactNumber; }
    public void setGuardianContactNumber(String guardianContactNumber) { this.guardianContactNumber = guardianContactNumber; }
    public String getGuardianAddress() { return guardianAddress; }
    public void setGuardianAddress(String guardianAddress) { this.guardianAddress = guardianAddress; }
    public String getHouseholdIncomeRange() { return householdIncomeRange; }
    public void setHouseholdIncomeRange(String householdIncomeRange) { this.householdIncomeRange = householdIncomeRange; }
}
