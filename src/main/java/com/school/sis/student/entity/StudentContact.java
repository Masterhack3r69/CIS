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
@Table(name = "student_contacts")
public class StudentContact extends AuditableEntity {
    @Id
    @Column(name = "student_id")
    private UUID studentId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @MapsId
    @JoinColumn(name = "student_id")
    private Student student;

    private String mobileNumber;
    private String telephoneNumber;
    private String emailAddress;
    private String currentAddress;
    private String permanentAddress;
    private String province;
    @Column(name = "city_municipality")
    private String cityMunicipality;
    private String barangay;
    private String zipCode;
    private String emergencyContactName;
    private String emergencyContactNumber;
    private String emergencyContactRelationship;
    private String emergencyContactAddress;

    public UUID getStudentId() { return studentId; }
    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }
    public String getMobileNumber() { return mobileNumber; }
    public void setMobileNumber(String mobileNumber) { this.mobileNumber = mobileNumber; }
    public String getTelephoneNumber() { return telephoneNumber; }
    public void setTelephoneNumber(String telephoneNumber) { this.telephoneNumber = telephoneNumber; }
    public String getEmailAddress() { return emailAddress; }
    public void setEmailAddress(String emailAddress) { this.emailAddress = emailAddress; }
    public String getCurrentAddress() { return currentAddress; }
    public void setCurrentAddress(String currentAddress) { this.currentAddress = currentAddress; }
    public String getPermanentAddress() { return permanentAddress; }
    public void setPermanentAddress(String permanentAddress) { this.permanentAddress = permanentAddress; }
    public String getProvince() { return province; }
    public void setProvince(String province) { this.province = province; }
    public String getCityMunicipality() { return cityMunicipality; }
    public void setCityMunicipality(String cityMunicipality) { this.cityMunicipality = cityMunicipality; }
    public String getBarangay() { return barangay; }
    public void setBarangay(String barangay) { this.barangay = barangay; }
    public String getZipCode() { return zipCode; }
    public void setZipCode(String zipCode) { this.zipCode = zipCode; }
    public String getEmergencyContactName() { return emergencyContactName; }
    public void setEmergencyContactName(String emergencyContactName) { this.emergencyContactName = emergencyContactName; }
    public String getEmergencyContactNumber() { return emergencyContactNumber; }
    public void setEmergencyContactNumber(String emergencyContactNumber) { this.emergencyContactNumber = emergencyContactNumber; }
    public String getEmergencyContactRelationship() { return emergencyContactRelationship; }
    public void setEmergencyContactRelationship(String emergencyContactRelationship) { this.emergencyContactRelationship = emergencyContactRelationship; }
    public String getEmergencyContactAddress() { return emergencyContactAddress; }
    public void setEmergencyContactAddress(String emergencyContactAddress) { this.emergencyContactAddress = emergencyContactAddress; }
}
