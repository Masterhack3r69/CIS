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
    private String currentRegionCode;
    private String currentRegionName;
    private String currentProvinceCode;
    private String currentProvinceName;
    private String currentCityMunicipalityCode;
    private String currentCityMunicipalityName;
    private String currentBarangayCode;
    private String currentBarangayName;
    private String currentZipCode;
    private String permanentRegionCode;
    private String permanentRegionName;
    private String permanentProvinceCode;
    private String permanentProvinceName;
    private String permanentCityMunicipalityCode;
    private String permanentCityMunicipalityName;
    private String permanentBarangayCode;
    private String permanentBarangayName;
    private String permanentZipCode;
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
    public String getCurrentRegionCode() { return currentRegionCode; }
    public void setCurrentRegionCode(String value) { this.currentRegionCode = value; }
    public String getCurrentRegionName() { return currentRegionName; }
    public void setCurrentRegionName(String value) { this.currentRegionName = value; }
    public String getCurrentProvinceCode() { return currentProvinceCode; }
    public void setCurrentProvinceCode(String value) { this.currentProvinceCode = value; }
    public String getCurrentProvinceName() { return currentProvinceName; }
    public void setCurrentProvinceName(String value) { this.currentProvinceName = value; }
    public String getCurrentCityMunicipalityCode() { return currentCityMunicipalityCode; }
    public void setCurrentCityMunicipalityCode(String value) { this.currentCityMunicipalityCode = value; }
    public String getCurrentCityMunicipalityName() { return currentCityMunicipalityName; }
    public void setCurrentCityMunicipalityName(String value) { this.currentCityMunicipalityName = value; }
    public String getCurrentBarangayCode() { return currentBarangayCode; }
    public void setCurrentBarangayCode(String value) { this.currentBarangayCode = value; }
    public String getCurrentBarangayName() { return currentBarangayName; }
    public void setCurrentBarangayName(String value) { this.currentBarangayName = value; }
    public String getCurrentZipCode() { return currentZipCode; }
    public void setCurrentZipCode(String value) { this.currentZipCode = value; }
    public String getPermanentRegionCode() { return permanentRegionCode; }
    public void setPermanentRegionCode(String value) { this.permanentRegionCode = value; }
    public String getPermanentRegionName() { return permanentRegionName; }
    public void setPermanentRegionName(String value) { this.permanentRegionName = value; }
    public String getPermanentProvinceCode() { return permanentProvinceCode; }
    public void setPermanentProvinceCode(String value) { this.permanentProvinceCode = value; }
    public String getPermanentProvinceName() { return permanentProvinceName; }
    public void setPermanentProvinceName(String value) { this.permanentProvinceName = value; }
    public String getPermanentCityMunicipalityCode() { return permanentCityMunicipalityCode; }
    public void setPermanentCityMunicipalityCode(String value) { this.permanentCityMunicipalityCode = value; }
    public String getPermanentCityMunicipalityName() { return permanentCityMunicipalityName; }
    public void setPermanentCityMunicipalityName(String value) { this.permanentCityMunicipalityName = value; }
    public String getPermanentBarangayCode() { return permanentBarangayCode; }
    public void setPermanentBarangayCode(String value) { this.permanentBarangayCode = value; }
    public String getPermanentBarangayName() { return permanentBarangayName; }
    public void setPermanentBarangayName(String value) { this.permanentBarangayName = value; }
    public String getPermanentZipCode() { return permanentZipCode; }
    public void setPermanentZipCode(String value) { this.permanentZipCode = value; }
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
