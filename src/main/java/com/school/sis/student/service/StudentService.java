package com.school.sis.student.service;

import com.school.sis.auth.entity.User;
import com.school.sis.auth.repository.UserRepository;
import com.school.sis.auth.security.SisUserDetails;
import com.school.sis.common.exception.BusinessRuleException;
import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.response.PageResponse;
import com.school.sis.curriculum.entity.Curriculum;
import com.school.sis.curriculum.repository.CurriculumRepository;
import com.school.sis.setup.entity.Program;
import com.school.sis.setup.entity.Section;
import com.school.sis.setup.repository.ProgramRepository;
import com.school.sis.setup.repository.SectionRepository;
import com.school.sis.student.dto.DocumentVerificationRequest;
import com.school.sis.student.dto.StudentAcademicRecordsResponse;
import com.school.sis.student.dto.StudentAcademicRequest;
import com.school.sis.student.dto.StudentAcademicResponse;
import com.school.sis.student.dto.StudentContactRequest;
import com.school.sis.student.dto.StudentContactResponse;
import com.school.sis.student.dto.StudentDocumentResponse;
import com.school.sis.student.dto.StudentEducationalRequest;
import com.school.sis.student.dto.StudentEducationalResponse;
import com.school.sis.student.dto.StudentFamilyRequest;
import com.school.sis.student.dto.StudentFamilyResponse;
import com.school.sis.student.dto.StudentPersonalRequest;
import com.school.sis.student.dto.StudentPersonalResponse;
import com.school.sis.student.dto.StudentRequest;
import com.school.sis.student.dto.StudentResponse;
import com.school.sis.student.dto.StudentSearchCriteria;
import com.school.sis.student.dto.StudentStatusRequest;
import com.school.sis.student.dto.StudentSummaryResponse;
import com.school.sis.student.entity.DocumentVerificationStatus;
import com.school.sis.student.entity.Student;
import com.school.sis.student.entity.StudentContact;
import com.school.sis.student.entity.StudentDocument;
import com.school.sis.student.entity.StudentEducationalBackground;
import com.school.sis.student.entity.StudentFamilyBackground;
import com.school.sis.student.repository.StudentContactRepository;
import com.school.sis.student.repository.StudentDocumentRepository;
import com.school.sis.student.repository.StudentEducationalBackgroundRepository;
import com.school.sis.student.repository.StudentFamilyBackgroundRepository;
import com.school.sis.student.repository.StudentRepository;
import jakarta.persistence.criteria.Subquery;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class StudentService {

    private final StudentRepository studentRepository;
    private final StudentContactRepository contactRepository;
    private final StudentFamilyBackgroundRepository familyRepository;
    private final StudentEducationalBackgroundRepository educationalRepository;
    private final StudentDocumentRepository documentRepository;
    private final ProgramRepository programRepository;
    private final CurriculumRepository curriculumRepository;
    private final SectionRepository sectionRepository;
    private final UserRepository userRepository;
    private final Path documentRoot;

    public StudentService(
            StudentRepository studentRepository,
            StudentContactRepository contactRepository,
            StudentFamilyBackgroundRepository familyRepository,
            StudentEducationalBackgroundRepository educationalRepository,
            StudentDocumentRepository documentRepository,
            ProgramRepository programRepository,
            CurriculumRepository curriculumRepository,
            SectionRepository sectionRepository,
            UserRepository userRepository,
            @Value("${sis.storage.document-root:uploads/documents}") String documentRoot
    ) {
        this.studentRepository = studentRepository;
        this.contactRepository = contactRepository;
        this.familyRepository = familyRepository;
        this.educationalRepository = educationalRepository;
        this.documentRepository = documentRepository;
        this.programRepository = programRepository;
        this.curriculumRepository = curriculumRepository;
        this.sectionRepository = sectionRepository;
        this.userRepository = userRepository;
        this.documentRoot = Paths.get(documentRoot).toAbsolutePath().normalize();
    }

    @Transactional(readOnly = true)
    public PageResponse<StudentSummaryResponse> list(StudentSearchCriteria criteria, Pageable pageable) {
        return PageResponse.from(studentRepository.findAll(specification(criteria), pageable).map(this::toSummary));
    }

    @Transactional(readOnly = true)
    public StudentResponse get(UUID id) {
        return toResponse(findStudent(id));
    }

    @Transactional
    public StudentResponse create(StudentRequest request) {
        validateUniqueStudentNumber(request.personal().studentNumber(), null);
        validateUniqueEmail(request.contact(), null);
        Student student = new Student();
        applyStudent(student, request);
        Student saved = studentRepository.save(student);
        saveCompanionRecords(saved, request);
        return toResponse(saved);
    }

    @Transactional
    public StudentResponse update(UUID id, StudentRequest request) {
        validateUniqueStudentNumber(request.personal().studentNumber(), id);
        validateUniqueEmail(request.contact(), id);
        Student student = findStudent(id);
        applyStudent(student, request);
        saveCompanionRecords(student, request);
        return toResponse(student);
    }

    @Transactional
    public StudentResponse updateStatus(UUID id, StudentStatusRequest request) {
        Student student = findStudent(id);
        student.setStatus(request.status());
        return toResponse(student);
    }

    @Transactional
    public StudentDocumentResponse uploadDocument(UUID studentId, String documentType, String remarks, MultipartFile file, SisUserDetails userDetails) {
        if (file == null || file.isEmpty()) {
            throw new BusinessRuleException("Document file is required");
        }
        if (documentType == null || documentType.isBlank()) {
            throw new BusinessRuleException("Document type is required");
        }
        Student student = findStudent(studentId);
        User uploader = findUser(userDetails);
        try {
            Path studentDirectory = documentRoot.resolve(studentId.toString()).normalize();
            Files.createDirectories(studentDirectory);
            String storedFileName = UUID.randomUUID() + "_" + sanitizeFileName(file.getOriginalFilename());
            Path target = studentDirectory.resolve(storedFileName).normalize();
            if (!target.startsWith(studentDirectory)) {
                throw new BusinessRuleException("Invalid file path");
            }
            file.transferTo(target);

            StudentDocument document = new StudentDocument();
            document.setStudent(student);
            document.setDocumentType(documentType);
            document.setFileName(file.getOriginalFilename() == null ? storedFileName : file.getOriginalFilename());
            document.setFilePath(target.toString());
            document.setMimeType(file.getContentType());
            document.setFileSize(file.getSize());
            document.setUploadedBy(uploader);
            document.setVerificationStatus(DocumentVerificationStatus.SUBMITTED);
            document.setRemarks(remarks);
            return toDocumentResponse(documentRepository.save(document));
        } catch (IOException exception) {
            throw new BusinessRuleException("Unable to store document file");
        }
    }

    @Transactional(readOnly = true)
    public List<StudentDocumentResponse> listDocuments(UUID studentId) {
        findStudent(studentId);
        return documentRepository.findByStudentIdOrderByCreatedAtDesc(studentId)
                .stream()
                .map(this::toDocumentResponse)
                .toList();
    }

    @Transactional
    public StudentDocumentResponse verifyDocument(UUID studentId, UUID documentId, DocumentVerificationRequest request, SisUserDetails userDetails) {
        StudentDocument document = documentRepository.findByIdAndStudentId(documentId, studentId)
                .orElseThrow(() -> new NotFoundException("Student document not found"));
        document.setVerificationStatus(request.status());
        document.setVerifiedBy(findUser(userDetails));
        document.setVerifiedAt(Instant.now());
        document.setRemarks(request.remarks());
        return toDocumentResponse(document);
    }

    @Transactional(readOnly = true)
    public StudentAcademicRecordsResponse academicRecords(UUID studentId) {
        Student student = findStudent(studentId);
        return new StudentAcademicRecordsResponse(
                student.getId(),
                student.getStudentNumber(),
                fullName(student),
                student.getProgram().getId(),
                student.getProgram().getProgramCode(),
                student.getCurriculum().getId(),
                student.getCurriculum().getCurriculumCode(),
                List.of()
        );
    }

    private void applyStudent(Student student, StudentRequest request) {
        StudentPersonalRequest personal = request.personal();
        StudentAcademicRequest academic = request.academic();
        Program program = programRepository.findById(academic.programId())
                .orElseThrow(() -> new NotFoundException("Program not found"));
        Curriculum curriculum = curriculumRepository.findById(academic.curriculumId())
                .orElseThrow(() -> new NotFoundException("Curriculum not found"));
        if (!curriculum.getProgram().getId().equals(program.getId())) {
            throw new BusinessRuleException("Curriculum does not belong to the selected program");
        }
        Section section = academic.sectionId() == null ? null : sectionRepository.findById(academic.sectionId())
                .orElseThrow(() -> new NotFoundException("Section not found"));

        student.setStudentNumber(personal.studentNumber());
        student.setFirstName(personal.firstName());
        student.setMiddleName(personal.middleName());
        student.setLastName(personal.lastName());
        student.setSuffix(personal.suffix());
        student.setGender(personal.gender());
        student.setBirthdate(personal.birthdate());
        student.setBirthplace(personal.birthplace());
        student.setCivilStatus(personal.civilStatus());
        student.setNationality(personal.nationality());
        student.setReligion(personal.religion());
        student.setProfilePhotoPath(personal.profilePhotoPath());
        student.setStatus(personal.status());
        student.setProgram(program);
        student.setCurriculum(curriculum);
        student.setYearLevel(academic.yearLevel());
        student.setSemester(academic.semester());
        student.setSection(section);
        student.setDateAdmitted(academic.dateAdmitted());
        student.setSchoolYearAdmitted(academic.schoolYearAdmitted());
        student.setClassification(academic.classification());
        student.setAcademicStatus(academic.academicStatus());
    }

    private void saveCompanionRecords(Student student, StudentRequest request) {
        StudentContact contact = contactRepository.findById(student.getId()).orElseGet(StudentContact::new);
        applyContact(contact, student, request.contact());
        contactRepository.save(contact);

        StudentFamilyBackground family = familyRepository.findById(student.getId()).orElseGet(StudentFamilyBackground::new);
        applyFamily(family, student, request.family());
        familyRepository.save(family);

        StudentEducationalBackground educational = educationalRepository.findById(student.getId()).orElseGet(StudentEducationalBackground::new);
        applyEducational(educational, student, request.educational());
        educationalRepository.save(educational);

        student.setContact(contact);
        student.setFamilyBackground(family);
        student.setEducationalBackground(educational);
    }

    private void applyContact(StudentContact contact, Student student, StudentContactRequest request) {
        contact.setStudent(student);
        if (request == null) return;
        contact.setMobileNumber(request.mobileNumber());
        contact.setTelephoneNumber(request.telephoneNumber());
        contact.setEmailAddress(blankToNull(request.emailAddress()));
        contact.setCurrentAddress(request.currentAddress());
        contact.setPermanentAddress(request.permanentAddress());
        contact.setProvince(request.province());
        contact.setCityMunicipality(request.cityMunicipality());
        contact.setBarangay(request.barangay());
        contact.setZipCode(request.zipCode());
        contact.setEmergencyContactName(request.emergencyContactName());
        contact.setEmergencyContactNumber(request.emergencyContactNumber());
        contact.setEmergencyContactRelationship(request.emergencyContactRelationship());
        contact.setEmergencyContactAddress(request.emergencyContactAddress());
    }

    private void applyFamily(StudentFamilyBackground family, Student student, StudentFamilyRequest request) {
        family.setStudent(student);
        if (request == null) return;
        family.setFatherName(request.fatherName());
        family.setFatherOccupation(request.fatherOccupation());
        family.setFatherContactNumber(request.fatherContactNumber());
        family.setMotherName(request.motherName());
        family.setMotherOccupation(request.motherOccupation());
        family.setMotherContactNumber(request.motherContactNumber());
        family.setGuardianName(request.guardianName());
        family.setGuardianRelationship(request.guardianRelationship());
        family.setGuardianContactNumber(request.guardianContactNumber());
        family.setGuardianAddress(request.guardianAddress());
        family.setHouseholdIncomeRange(request.householdIncomeRange());
    }

    private void applyEducational(StudentEducationalBackground educational, Student student, StudentEducationalRequest request) {
        educational.setStudent(student);
        if (request == null) return;
        educational.setElementarySchoolName(request.elementarySchoolName());
        educational.setElementarySchoolAddress(request.elementarySchoolAddress());
        educational.setElementaryYearGraduated(request.elementaryYearGraduated());
        educational.setJuniorHighSchoolName(request.juniorHighSchoolName());
        educational.setJuniorHighSchoolAddress(request.juniorHighSchoolAddress());
        educational.setJuniorHighSchoolYearGraduated(request.juniorHighSchoolYearGraduated());
        educational.setSeniorHighSchoolName(request.seniorHighSchoolName());
        educational.setSeniorHighSchoolAddress(request.seniorHighSchoolAddress());
        educational.setSeniorHighSchoolStrand(request.seniorHighSchoolStrand());
        educational.setSeniorHighSchoolYearGraduated(request.seniorHighSchoolYearGraduated());
        educational.setPreviousCollege(request.previousCollege());
        educational.setPreviousProgram(request.previousProgram());
        educational.setPreviousSchoolYearAttended(request.previousSchoolYearAttended());
        educational.setAdmissionType(request.admissionType());
    }

    private void validateUniqueStudentNumber(String studentNumber, UUID currentStudentId) {
        boolean exists = currentStudentId == null
                ? studentRepository.existsByStudentNumberIgnoreCase(studentNumber)
                : studentRepository.existsByStudentNumberIgnoreCaseAndIdNot(studentNumber, currentStudentId);
        if (exists) throw new BusinessRuleException("Student number already exists");
    }

    private void validateUniqueEmail(StudentContactRequest contact, UUID currentStudentId) {
        if (contact == null || blankToNull(contact.emailAddress()) == null) return;
        boolean exists = currentStudentId == null
                ? contactRepository.existsByEmailAddressIgnoreCase(contact.emailAddress())
                : contactRepository.existsByEmailAddressIgnoreCaseAndStudentIdNot(contact.emailAddress(), currentStudentId);
        if (exists) throw new BusinessRuleException("Student email already exists");
    }

    private Student findStudent(UUID id) {
        return studentRepository.findById(id).orElseThrow(() -> new NotFoundException("Student not found"));
    }

    private User findUser(SisUserDetails userDetails) {
        if (userDetails == null) return null;
        return userRepository.findById(userDetails.id()).orElse(null);
    }

    private Specification<Student> specification(StudentSearchCriteria criteria) {
        return (root, query, cb) -> {
            var predicate = cb.conjunction();
            if (criteria.search() != null && !criteria.search().isBlank()) {
                String term = "%" + criteria.search().toLowerCase(Locale.ROOT) + "%";
                predicate = cb.and(predicate, cb.or(
                        cb.like(cb.lower(root.get("studentNumber")), term),
                        cb.like(cb.lower(root.get("firstName")), term),
                        cb.like(cb.lower(root.get("lastName")), term)
                ));
            }
            if (criteria.programId() != null) predicate = cb.and(predicate, cb.equal(root.get("program").get("id"), criteria.programId()));
            if (criteria.yearLevel() != null) predicate = cb.and(predicate, cb.equal(root.get("yearLevel"), criteria.yearLevel()));
            if (criteria.sectionId() != null) predicate = cb.and(predicate, cb.equal(root.get("section").get("id"), criteria.sectionId()));
            if (criteria.status() != null) predicate = cb.and(predicate, cb.equal(root.get("status"), criteria.status()));
            if (criteria.schoolYearAdmitted() != null && !criteria.schoolYearAdmitted().isBlank()) {
                predicate = cb.and(predicate, cb.equal(root.get("schoolYearAdmitted"), criteria.schoolYearAdmitted()));
            }
            if (criteria.documentStatus() != null) {
                Subquery<UUID> subquery = query.subquery(UUID.class);
                var document = subquery.from(StudentDocument.class);
                subquery.select(document.get("student").get("id"))
                        .where(cb.equal(document.get("verificationStatus"), criteria.documentStatus()));
                predicate = cb.and(predicate, root.get("id").in(subquery));
            }
            return predicate;
        };
    }

    private StudentResponse toResponse(Student student) {
        return new StudentResponse(
                toPersonalResponse(student),
                toContactResponse(student.getContact()),
                toFamilyResponse(student.getFamilyBackground()),
                toEducationalResponse(student.getEducationalBackground()),
                toAcademicResponse(student)
        );
    }

    private StudentSummaryResponse toSummary(Student student) {
        return new StudentSummaryResponse(
                student.getId(),
                student.getStudentNumber(),
                fullName(student),
                student.getContact() == null ? null : student.getContact().getEmailAddress(),
                student.getProgram().getId(),
                student.getProgram().getProgramCode(),
                student.getYearLevel(),
                student.getSection() == null ? null : student.getSection().getId(),
                student.getSection() == null ? null : student.getSection().getSectionCode(),
                student.getStatus(),
                student.getSchoolYearAdmitted()
        );
    }

    private StudentPersonalResponse toPersonalResponse(Student student) {
        return new StudentPersonalResponse(
                student.getId(),
                student.getStudentNumber(),
                student.getFirstName(),
                student.getMiddleName(),
                student.getLastName(),
                student.getSuffix(),
                fullName(student),
                student.getGender(),
                student.getBirthdate(),
                student.getBirthplace(),
                student.getCivilStatus(),
                student.getNationality(),
                student.getReligion(),
                student.getProfilePhotoPath(),
                student.getStatus()
        );
    }

    private StudentContactResponse toContactResponse(StudentContact contact) {
        if (contact == null) return null;
        return new StudentContactResponse(contact.getMobileNumber(), contact.getTelephoneNumber(), contact.getEmailAddress(),
                contact.getCurrentAddress(), contact.getPermanentAddress(), contact.getProvince(), contact.getCityMunicipality(),
                contact.getBarangay(), contact.getZipCode(), contact.getEmergencyContactName(), contact.getEmergencyContactNumber(),
                contact.getEmergencyContactRelationship(), contact.getEmergencyContactAddress());
    }

    private StudentFamilyResponse toFamilyResponse(StudentFamilyBackground family) {
        if (family == null) return null;
        return new StudentFamilyResponse(family.getFatherName(), family.getFatherOccupation(), family.getFatherContactNumber(),
                family.getMotherName(), family.getMotherOccupation(), family.getMotherContactNumber(), family.getGuardianName(),
                family.getGuardianRelationship(), family.getGuardianContactNumber(), family.getGuardianAddress(), family.getHouseholdIncomeRange());
    }

    private StudentEducationalResponse toEducationalResponse(StudentEducationalBackground educational) {
        if (educational == null) return null;
        return new StudentEducationalResponse(educational.getElementarySchoolName(), educational.getElementarySchoolAddress(),
                educational.getElementaryYearGraduated(), educational.getJuniorHighSchoolName(), educational.getJuniorHighSchoolAddress(),
                educational.getJuniorHighSchoolYearGraduated(), educational.getSeniorHighSchoolName(), educational.getSeniorHighSchoolAddress(),
                educational.getSeniorHighSchoolStrand(), educational.getSeniorHighSchoolYearGraduated(), educational.getPreviousCollege(),
                educational.getPreviousProgram(), educational.getPreviousSchoolYearAttended(), educational.getAdmissionType());
    }

    private StudentAcademicResponse toAcademicResponse(Student student) {
        return new StudentAcademicResponse(student.getProgram().getId(), student.getProgram().getProgramCode(),
                student.getCurriculum().getId(), student.getCurriculum().getCurriculumCode(), student.getYearLevel(),
                student.getSemester(), student.getSection() == null ? null : student.getSection().getId(),
                student.getSection() == null ? null : student.getSection().getSectionCode(), student.getDateAdmitted(),
                student.getSchoolYearAdmitted(), student.getClassification(), student.getAcademicStatus());
    }

    private StudentDocumentResponse toDocumentResponse(StudentDocument document) {
        return new StudentDocumentResponse(document.getId(), document.getStudent().getId(), document.getDocumentType(),
                document.getFileName(), document.getFilePath(), document.getMimeType(), document.getFileSize(),
                document.getUploadedBy() == null ? null : document.getUploadedBy().getId(), document.getVerificationStatus(),
                document.getVerifiedBy() == null ? null : document.getVerifiedBy().getId(), document.getVerifiedAt(), document.getRemarks());
    }

    private String fullName(Student student) {
        return String.join(" ", List.of(student.getFirstName(), blankToEmpty(student.getMiddleName()), student.getLastName(), blankToEmpty(student.getSuffix())))
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String sanitizeFileName(String fileName) {
        String fallback = fileName == null || fileName.isBlank() ? "document" : fileName;
        return fallback.replaceAll("[^A-Za-z0-9._-]", "_");
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private String blankToEmpty(String value) {
        return value == null ? "" : value;
    }
}
