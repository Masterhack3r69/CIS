package com.school.sis.student.entity;

import com.school.sis.common.audit.AuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapsId;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "student_educational_backgrounds")
public class StudentEducationalBackground extends AuditableEntity {
    @Id
    @Column(name = "student_id")
    private UUID studentId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "student_id")
    private Student student;

    private String elementarySchoolName;
    private String elementarySchoolAddress;
    private Integer elementaryYearGraduated;
    private String juniorHighSchoolName;
    private String juniorHighSchoolAddress;
    private Integer juniorHighSchoolYearGraduated;
    private String seniorHighSchoolName;
    private String seniorHighSchoolAddress;
    private String seniorHighSchoolStrand;
    private Integer seniorHighSchoolYearGraduated;
    private String previousCollege;
    private String previousProgram;
    private String previousSchoolYearAttended;

    @Enumerated(EnumType.STRING)
    private AdmissionType admissionType;

    public UUID getStudentId() { return studentId; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public String getElementarySchoolName() { return elementarySchoolName; }
    public void setElementarySchoolName(String elementarySchoolName) { this.elementarySchoolName = elementarySchoolName; }
    public String getElementarySchoolAddress() { return elementarySchoolAddress; }
    public void setElementarySchoolAddress(String elementarySchoolAddress) { this.elementarySchoolAddress = elementarySchoolAddress; }
    public Integer getElementaryYearGraduated() { return elementaryYearGraduated; }
    public void setElementaryYearGraduated(Integer elementaryYearGraduated) { this.elementaryYearGraduated = elementaryYearGraduated; }
    public String getJuniorHighSchoolName() { return juniorHighSchoolName; }
    public void setJuniorHighSchoolName(String juniorHighSchoolName) { this.juniorHighSchoolName = juniorHighSchoolName; }
    public String getJuniorHighSchoolAddress() { return juniorHighSchoolAddress; }
    public void setJuniorHighSchoolAddress(String juniorHighSchoolAddress) { this.juniorHighSchoolAddress = juniorHighSchoolAddress; }
    public Integer getJuniorHighSchoolYearGraduated() { return juniorHighSchoolYearGraduated; }
    public void setJuniorHighSchoolYearGraduated(Integer juniorHighSchoolYearGraduated) { this.juniorHighSchoolYearGraduated = juniorHighSchoolYearGraduated; }
    public String getSeniorHighSchoolName() { return seniorHighSchoolName; }
    public void setSeniorHighSchoolName(String seniorHighSchoolName) { this.seniorHighSchoolName = seniorHighSchoolName; }
    public String getSeniorHighSchoolAddress() { return seniorHighSchoolAddress; }
    public void setSeniorHighSchoolAddress(String seniorHighSchoolAddress) { this.seniorHighSchoolAddress = seniorHighSchoolAddress; }
    public String getSeniorHighSchoolStrand() { return seniorHighSchoolStrand; }
    public void setSeniorHighSchoolStrand(String seniorHighSchoolStrand) { this.seniorHighSchoolStrand = seniorHighSchoolStrand; }
    public Integer getSeniorHighSchoolYearGraduated() { return seniorHighSchoolYearGraduated; }
    public void setSeniorHighSchoolYearGraduated(Integer seniorHighSchoolYearGraduated) { this.seniorHighSchoolYearGraduated = seniorHighSchoolYearGraduated; }
    public String getPreviousCollege() { return previousCollege; }
    public void setPreviousCollege(String previousCollege) { this.previousCollege = previousCollege; }
    public String getPreviousProgram() { return previousProgram; }
    public void setPreviousProgram(String previousProgram) { this.previousProgram = previousProgram; }
    public String getPreviousSchoolYearAttended() { return previousSchoolYearAttended; }
    public void setPreviousSchoolYearAttended(String previousSchoolYearAttended) { this.previousSchoolYearAttended = previousSchoolYearAttended; }
    public AdmissionType getAdmissionType() { return admissionType; }
    public void setAdmissionType(AdmissionType admissionType) { this.admissionType = admissionType; }
}
