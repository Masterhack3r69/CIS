package com.school.sis.setup.entity;

import com.school.sis.common.audit.AuditableEntity;
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

import java.util.UUID;

@Entity
@Table(name = "faculty")
public class Faculty extends AuditableEntity {
    @Id
    private UUID id;

    @Column(name = "employee_number", nullable = false, unique = true, length = 60)
    private String employeeNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private String suffix;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "contact_number")
    private String contactNumber;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "department_id", nullable = false)
    private Department department;

    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false)
    private EmploymentStatus employmentStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "faculty_type", nullable = false)
    private FacultyType facultyType;

    private String specialization;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActiveStatus status = ActiveStatus.ACTIVE;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    public UUID getId() { return id; }
    public String getEmployeeNumber() { return employeeNumber; }
    public void setEmployeeNumber(String employeeNumber) { this.employeeNumber = employeeNumber; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getContactNumber() { return contactNumber; }
    public void setContactNumber(String contactNumber) { this.contactNumber = contactNumber; }
    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }
    public EmploymentStatus getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(EmploymentStatus employmentStatus) { this.employmentStatus = employmentStatus; }
    public FacultyType getFacultyType() { return facultyType; }
    public void setFacultyType(FacultyType facultyType) { this.facultyType = facultyType; }
    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }
    public ActiveStatus getStatus() { return status; }
    public void setStatus(ActiveStatus status) { this.status = status; }
}
