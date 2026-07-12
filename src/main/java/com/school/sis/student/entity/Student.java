package com.school.sis.student.entity;

import com.school.sis.common.audit.AuditableEntity;
import com.school.sis.curriculum.entity.Curriculum;
import com.school.sis.setup.entity.Program;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "students")
public class Student extends AuditableEntity {
    @Id
    private UUID id;

    @Column(name = "student_number", nullable = false, unique = true, length = 60)
    private String studentNumber;

    @Column(name = "first_name", nullable = false)
    private String firstName;

    @Column(name = "middle_name")
    private String middleName;

    @Column(name = "last_name", nullable = false)
    private String lastName;

    private String suffix;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Column(nullable = false)
    private LocalDate birthdate;

    private String birthplace;

    @Column(name = "civil_status")
    private String civilStatus;

    private String nationality;
    private String religion;

    @Column(name = "profile_photo_path")
    private String profilePhotoPath;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StudentStatus status;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "program_id", nullable = false)
    private Program program;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "curriculum_id", nullable = false)
    private Curriculum curriculum;

    @Column(name = "year_level", nullable = false)
    private int yearLevel;

    @Column(name = "date_admitted", nullable = false)
    private LocalDate dateAdmitted;

    @Column(name = "school_year_admitted", nullable = false, length = 20)
    private String schoolYearAdmitted;

    @Enumerated(EnumType.STRING)
    private StudentClassification classification;

    @Enumerated(EnumType.STRING)
    @Column(name = "academic_status")
    private AcademicStatus academicStatus;

    @OneToOne(mappedBy = "student", fetch = FetchType.LAZY)
    private StudentContact contact;

    @OneToOne(mappedBy = "student", fetch = FetchType.LAZY)
    private StudentFamilyBackground familyBackground;

    @OneToOne(mappedBy = "student", fetch = FetchType.LAZY)
    private StudentEducationalBackground educationalBackground;

    @PrePersist
    void prePersist() {
        if (id == null) id = UUID.randomUUID();
    }

    public UUID getId() { return id; }
    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }
    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getMiddleName() { return middleName; }
    public void setMiddleName(String middleName) { this.middleName = middleName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getSuffix() { return suffix; }
    public void setSuffix(String suffix) { this.suffix = suffix; }
    public Gender getGender() { return gender; }
    public void setGender(Gender gender) { this.gender = gender; }
    public LocalDate getBirthdate() { return birthdate; }
    public void setBirthdate(LocalDate birthdate) { this.birthdate = birthdate; }
    public String getBirthplace() { return birthplace; }
    public void setBirthplace(String birthplace) { this.birthplace = birthplace; }
    public String getCivilStatus() { return civilStatus; }
    public void setCivilStatus(String civilStatus) { this.civilStatus = civilStatus; }
    public String getNationality() { return nationality; }
    public void setNationality(String nationality) { this.nationality = nationality; }
    public String getReligion() { return religion; }
    public void setReligion(String religion) { this.religion = religion; }
    public String getProfilePhotoPath() { return profilePhotoPath; }
    public void setProfilePhotoPath(String profilePhotoPath) { this.profilePhotoPath = profilePhotoPath; }
    public StudentStatus getStatus() { return status; }
    public void setStatus(StudentStatus status) { this.status = status; }
    public Program getProgram() { return program; }
    public void setProgram(Program program) { this.program = program; }
    public Curriculum getCurriculum() { return curriculum; }
    public void setCurriculum(Curriculum curriculum) { this.curriculum = curriculum; }
    public int getYearLevel() { return yearLevel; }
    public void setYearLevel(int yearLevel) { this.yearLevel = yearLevel; }
    public LocalDate getDateAdmitted() { return dateAdmitted; }
    public void setDateAdmitted(LocalDate dateAdmitted) { this.dateAdmitted = dateAdmitted; }
    public String getSchoolYearAdmitted() { return schoolYearAdmitted; }
    public void setSchoolYearAdmitted(String schoolYearAdmitted) { this.schoolYearAdmitted = schoolYearAdmitted; }
    public StudentClassification getClassification() { return classification; }
    public void setClassification(StudentClassification classification) { this.classification = classification; }
    public AcademicStatus getAcademicStatus() { return academicStatus; }
    public void setAcademicStatus(AcademicStatus academicStatus) { this.academicStatus = academicStatus; }
    public StudentContact getContact() { return contact; }
    public void setContact(StudentContact contact) { this.contact = contact; }
    public StudentFamilyBackground getFamilyBackground() { return familyBackground; }
    public void setFamilyBackground(StudentFamilyBackground familyBackground) { this.familyBackground = familyBackground; }
    public StudentEducationalBackground getEducationalBackground() { return educationalBackground; }
    public void setEducationalBackground(StudentEducationalBackground educationalBackground) { this.educationalBackground = educationalBackground; }
}
