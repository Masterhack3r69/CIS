# Student Profiling Module Exploration Report

## Overview
This report contains an in-depth analysis of the Student Profiling backend module of the system, covering exposed API endpoints, entity structures, DTO mappings, database schema, document management rules, and core business logic constraint rules.

---

## 1. REST API Endpoints

All endpoints are prefixed with `/api/v1/students`.

| Method | Path | Required Permission | Request Type | Request Parameters / Parts / Body Schema | Response Body Schema | Description |
|---|---|---|---|---|---|---|
| **GET** | `/` | `STUDENT_VIEW` | Query Parameters | `search` (String, optional)<br>`programId` (UUID, optional)<br>`yearLevel` (Integer, optional)<br>`sectionId` (UUID, optional)<br>`status` (StudentStatus, optional)<br>`schoolYearAdmitted` (String, optional)<br>`documentStatus` (DocumentVerificationStatus, optional)<br>Plus standard pagination query params (`page`, `size`, `sort`) | `ApiResponse<PageResponse<StudentSummaryResponse>>` | Retrieves a paginated list of students filtered by the given search criteria. |
| **POST** | `/` | `STUDENT_CREATE` | JSON Body | `StudentRequest` (nested DTOs: `personal`, `contact`, `family`, `educational`, `academic`) | `ApiResponse<StudentResponse>` | Creates a new student profile and its companion records (contact, family background, and educational background). |
| **GET** | `/{id}` | `STUDENT_VIEW` | Path Variable | `id` (UUID, required) | `ApiResponse<StudentResponse>` | Retrieves the detailed profile of a student. |
| **PUT** | `/{id}` | `STUDENT_UPDATE` | Path Variable + JSON Body | `id` (UUID, required)<br>`StudentRequest` (nested DTOs) | `ApiResponse<StudentResponse>` | Updates an existing student profile and its companion records. |
| **PATCH** | `/{id}/status` | `STUDENT_UPDATE` | Path Variable + JSON Body | `id` (UUID, required)<br>`StudentStatusRequest` (`status` StudentStatus, required) | `ApiResponse<StudentResponse>` | Updates the profile status of a student (e.g. APPLICANT to ACTIVE). |
| **POST** | `/{id}/documents` | `STUDENT_UPDATE` | Path Variable + Form Params + Multipart | `id` (UUID, required)<br>`documentType` (String, required query parameter)<br>`remarks` (String, optional query parameter)<br>`file` (MultipartFile, required part) | `ApiResponse<StudentDocumentResponse>` | Uploads and registers a verification document for a student. |
| **GET** | `/{id}/documents` | `STUDENT_VIEW` | Path Variable | `id` (UUID, required) | `ApiResponse<List<StudentDocumentResponse>>` | Retrieves the list of documents uploaded for a specific student, sorted by creation date descending. |
| **PATCH** | `/{id}/documents/{documentId}/verify` | `STUDENT_UPDATE` | Path Variable + JSON Body | `id` (UUID, required)<br>`documentId` (UUID, required)<br>`DocumentVerificationRequest` (`status` DocumentVerificationStatus, optional `remarks`) | `ApiResponse<StudentDocumentResponse>` | Updates the verification status of a specific document (e.g., VERIFIED, REJECTED). |
| **GET** | `/{id}/academic-records` | `STUDENT_VIEW` | Path Variable | `id` (UUID, required) | `ApiResponse<StudentAcademicRecordsResponse>` | Retrieves a student's academic transcript/record including courses, grades, credits, semesters, and sections. |

---

## 2. Entity and DTO Field Mappings

### 2.1. Student Entity (Database Table: `students`)
Extends `AuditableEntity` (inheriting `created_at` and `updated_at` as `TIMESTAMPTZ NOT NULL`).

| Java Field | Database Column | Data Type | Nullability | Constraints / Validations / Notes |
|---|---|---|---|---|
| `id` | `id` | UUID | NOT NULL | Primary Key. Automatically generated if null via `@PrePersist`. |
| `studentNumber` | `student_number` | VARCHAR(60) | NOT NULL | Unique, Case-insensitive unique validation on backend. |
| `firstName` | `first_name` | TEXT | NOT NULL | |
| `middleName` | `middle_name` | TEXT | NULL | Optional. |
| `lastName` | `last_name` | TEXT | NOT NULL | |
| `suffix` | `suffix` | TEXT | NULL | Optional. |
| `gender` | `gender` | VARCHAR(20) | NULL | Maps to `Gender` enum (`MALE`, `FEMALE`, `OTHER`). |
| `birthdate` | `birthdate` | DATE | NOT NULL | |
| `birthplace` | `birthplace` | TEXT | NULL | Optional. |
| `civilStatus` | `civil_status` | VARCHAR(40) | NULL | Optional. |
| `nationality` | `nationality` | VARCHAR(80) | NULL | Optional. |
| `religion` | `religion` | VARCHAR(80) | NULL | Optional. |
| `profilePhotoPath` | `profile_photo_path` | TEXT | NULL | Optional. |
| `status` | `status` | VARCHAR(40) | NOT NULL | Maps to `StudentStatus` enum (`APPLICANT`, `ACTIVE`, `ENROLLED`, `INACTIVE`, `DROPPED`, `TRANSFERRED`, `GRADUATED`, `ARCHIVED`). |
| `program` | `program_id` | UUID | NOT NULL | Foreign key referencing `programs(id)`. |
| `curriculum` | `curriculum_id` | UUID | NOT NULL | Foreign key referencing `curricula(id)`. |
| `yearLevel` | `year_level` | INTEGER | NOT NULL | Check constraint: `students_year_level_positive CHECK (year_level > 0)`. |
| `semester` | `semester` | VARCHAR(40) | NULL | Optional. |
| `section` | `section_id` | UUID | NULL | Foreign key referencing `sections(id)`. |
| `dateAdmitted` | `date_admitted` | DATE | NOT NULL | |
| `schoolYearAdmitted`| `school_year_admitted`| VARCHAR(20) | NOT NULL | |
| `classification` | `classification` | VARCHAR(40) | NULL | Maps to `StudentClassification` enum (`REGULAR`, `IRREGULAR`, `TRANSFEREE`, `RETURNEE`, `CROSS_ENROLLEE`, `GRADUATING`). |
| `academicStatus` | `academic_status` | VARCHAR(40) | NULL | Maps to `AcademicStatus` enum (`REGULAR`, `IRREGULAR`, `PROBATION`, `CANDIDATE_FOR_GRADUATION`, `GRADUATED`, `DISMISSED`, `ON_LEAVE`). |

---

### 2.2. Student Contact (Database Table: `student_contacts`)
Shares a shared primary key with `students` via `@MapsId`. Cascades delete with `students`.

| Java Field / DB Column | Data Type | Nullability | Constraints / Validations / Notes |
|---|---|---|---|
| `studentId` / `student_id` | UUID | NOT NULL | Primary Key and Foreign Key referencing `students(id)`. |
| `mobileNumber` / `mobile_number` | VARCHAR(40) | NULL | |
| `telephoneNumber` / `telephone_number`| VARCHAR(40) | NULL | |
| `emailAddress` / `email_address` | VARCHAR(120)| NULL | Unique. Checked on backend for case-insensitive uniqueness. |
| `currentAddress` / `current_address` | TEXT | NULL | |
| `permanentAddress` / `permanent_address`| TEXT | NULL | |
| `province` / `province` | TEXT | NULL | |
| `cityMunicipality` / `city_municipality`| TEXT | NULL | |
| `barangay` / `barangay` | TEXT | NULL | |
| `zipCode` / `zip_code` | VARCHAR(20) | NULL | |
| `emergencyContactName` / `emergency_contact_name` | TEXT | NULL | Emergency contact person name. |
| `emergencyContactNumber` / `emergency_contact_number` | VARCHAR(40) | NULL | Emergency contact phone. |
| `emergencyContactRelationship` / `emergency_contact_relationship` | TEXT | NULL | Relationship to student. |
| `emergencyContactAddress` / `emergency_contact_address` | TEXT | NULL | Emergency contact address. |

---

### 2.3. Student Family Background (Database Table: `student_family_backgrounds`)
Shares a shared primary key with `students` via `@MapsId`. Cascades delete with `students`.

| Java Field / DB Column | Data Type | Nullability | Constraints / Notes |
|---|---|---|---|
| `studentId` / `student_id` | UUID | NOT NULL | Primary Key and Foreign Key referencing `students(id)`. |
| `fatherName` / `father_name` | TEXT | NULL | |
| `fatherOccupation` / `father_occupation` | TEXT | NULL | |
| `fatherContactNumber` / `father_contact_number`| VARCHAR(40) | NULL | |
| `motherName` / `mother_name` | TEXT | NULL | |
| `motherOccupation` / `mother_occupation` | TEXT | NULL | |
| `motherContactNumber` / `mother_contact_number`| VARCHAR(40) | NULL | |
| `guardianName` / `guardian_name` | TEXT | NULL | |
| `guardianRelationship` / `guardian_relationship`| TEXT | NULL | |
| `guardianContactNumber` / `guardian_contact_number` | VARCHAR(40) | NULL | |
| `guardianAddress` / `guardian_address` | TEXT | NULL | |
| `householdIncomeRange` / `household_income_range`| TEXT | NULL | |

---

### 2.4. Student Educational Background (Database Table: `student_educational_backgrounds`)
Shares a shared primary key with `students` via `@MapsId`. Cascades delete with `students`.

| Java Field / DB Column | Data Type | Nullability | Constraints / Notes |
|---|---|---|---|
| `studentId` / `student_id` | UUID | NOT NULL | Primary Key and Foreign Key referencing `students(id)`. |
| `elementarySchoolName` / `elementary_school_name` | TEXT | NULL | |
| `elementarySchoolAddress` / `elementary_school_address` | TEXT | NULL | |
| `elementaryYearGraduated` / `elementary_year_graduated` | INTEGER | NULL | |
| `juniorHighSchoolName` / `junior_high_school_name` | TEXT | NULL | |
| `juniorHighSchoolAddress` / `junior_high_school_address` | TEXT | NULL | |
| `juniorHighSchoolYearGraduated` / `junior_high_school_year_graduated`| INTEGER | NULL | |
| `seniorHighSchoolName` / `senior_high_school_name` | TEXT | NULL | |
| `seniorHighSchoolAddress` / `senior_high_school_address` | TEXT | NULL | |
| `seniorHighSchoolStrand` / `senior_high_school_strand` | TEXT | NULL | |
| `seniorHighSchoolYearGraduated` / `senior_high_school_year_graduated`| INTEGER | NULL | |
| `previousCollege` / `previous_college` | TEXT | NULL | For transferees/graduates. |
| `previousProgram` / `previous_program` | TEXT | NULL | |
| `previousSchoolYearAttended` / `previous_school_year_attended` | TEXT | NULL | |
| `admissionType` / `admission_type` | VARCHAR(40) | NULL | Maps to `AdmissionType` enum (`NEW_STUDENT`, `TRANSFEREE`, `RETURNEE`, `SHIFTEE`, `CROSS_ENROLLEE`, `CONTINUING_STUDENT`). |

---

### 2.5. Student Document Entity (Database Table: `student_documents`)
Registers external files. Cascades delete with `students`.

| Java Field | Database Column | Data Type | Nullability | Constraints / Validations / Notes |
|---|---|---|---|---|
| `id` | `id` | UUID | NOT NULL | Primary Key. Automatically generated if null via `@PrePersist`. |
| `student` | `student_id` | UUID | NOT NULL | Foreign key referencing `students(id)`. |
| `documentType` | `document_type` | VARCHAR(120) | NOT NULL | |
| `fileName` | `file_name` | TEXT | NOT NULL | Original filename. |
| `filePath` | `file_path` | TEXT | NOT NULL | Path on the backend filesystem. |
| `mimeType` | `mime_type` | VARCHAR(160) | NULL | |
| `fileSize` | `file_size` | BIGINT | NOT NULL | size in bytes. |
| `uploadedBy` | `uploaded_by` | UUID | NULL | Foreign key referencing `users(id)`. |
| `verificationStatus`| `verification_status`| VARCHAR(40) | NOT NULL | Maps to `DocumentVerificationStatus` enum (`PENDING`, `SUBMITTED`, `VERIFIED`, `REJECTED`, `MISSING`). Default: `SUBMITTED`. |
| `verifiedBy` | `verified_by` | UUID | NULL | Foreign key referencing `users(id)`. |
| `verifiedAt` | `verified_at` | TIMESTAMPTZ | NULL | Timestamp when verified. |
| `remarks` | `remarks` | TEXT | NULL | |

---

### 2.6. Request Validation Constraints (DTO Layer)

#### `StudentRequest`
- `personal`: `@Valid @NotNull StudentPersonalRequest`
- `contact`: `@Valid StudentContactRequest` (optional)
- `family`: `StudentFamilyRequest` (optional)
- `educational`: `StudentEducationalRequest` (optional)
- `academic`: `@Valid @NotNull StudentAcademicRequest`

#### `StudentPersonalRequest`
- `studentNumber`: `@NotBlank String`
- `firstName`: `@NotBlank String`
- `middleName`: `String` (optional)
- `lastName`: `@NotBlank String`
- `suffix`: `String` (optional)
- `gender`: `Gender` enum (optional)
- `birthdate`: `@NotNull LocalDate`
- `birthplace`: `String` (optional)
- `civilStatus`: `String` (optional)
- `nationality`: `String` (optional)
- `religion`: `String` (optional)
- `profilePhotoPath`: `String` (optional)
- `status`: `@NotNull StudentStatus`

#### `StudentContactRequest`
- `emailAddress`: `@Email String` (validated to have correct syntax if present; can be empty/blank/null).
- All other fields (`mobileNumber`, `telephoneNumber`, `currentAddress`, etc.) are optional `String`s.

#### `StudentAcademicRequest`
- `programId`: `@NotNull UUID`
- `curriculumId`: `@NotNull UUID`
- `yearLevel`: `@Min(1) int` (validated to be positive)
- `semester`: `String` (optional)
- `sectionId`: `UUID` (optional)
- `dateAdmitted`: `@NotNull LocalDate`
- `schoolYearAdmitted`: `@NotBlank String`
- `classification`: `StudentClassification` enum (optional)
- `academicStatus`: `AcademicStatus` enum (optional)

#### `StudentStatusRequest`
- `status`: `@NotNull StudentStatus`

#### `DocumentVerificationRequest`
- `status`: `@NotNull DocumentVerificationStatus`
- `remarks`: `String` (optional)

---

## 3. Document Upload and Verification Rules

### 3.1. Document Upload Process (`StudentService.uploadDocument`)
1. **Mandatory Checks**:
   - The file must not be null or empty, otherwise throws `BusinessRuleException("Document file is required")`.
   - The document type must not be null or blank, otherwise throws `BusinessRuleException("Document type is required")`.
2. **Directory & File Resolution**:
   - The root document storage folder is configured via the property `sis.storage.document-root` (default value: `uploads/documents`).
   - A dedicated folder is resolved and created for the student: `${documentRoot}/${studentId}/`.
   - The uploaded file's original name is sanitized (characters other than `[A-Za-z0-9._-]` are replaced with `_`).
   - A unique filename is created by prefixing the sanitized name with a random UUID: `UUID.randomUUID() + "_" + sanitizedName`.
3. **Security Check (Path Traversal)**:
   - The system checks if the normalized resolved target file path starts with the student's sub-directory. If not, a `BusinessRuleException("Invalid file path")` is thrown.
4. **Storage & Database Entry**:
   - The file is saved to the disk.
   - A `StudentDocument` record is saved in the database with status initialized to `DocumentVerificationStatus.SUBMITTED`.
   - The ID of the authenticated user who uploaded the file is recorded (`uploaded_by`).
5. **Auditing**:
   - A log event `DOCUMENT_UPLOADED` is registered containing the student ID, document type, and file name.

### 3.2. Document Verification Process (`StudentService.verifyDocument`)
1. **Resolution**:
   - The document must belong to the specified student (searched via `findByIdAndStudentId`). Otherwise throws `NotFoundException("Student document not found")`.
2. **State Updates**:
   - The status is updated to the requested verification status (`PENDING`, `SUBMITTED`, `VERIFIED`, `REJECTED`, `MISSING`).
   - The verifying user details and current time (`Instant.now()`) are stamped as `verified_by` and `verified_at` respectively.
   - Any comments are stored under `remarks`.
3. **Auditing**:
   - A log event `DOCUMENT_VERIFIED` is registered, capturing both the old and new verification statuses.

---

## 4. Backend Business Rules and Logic Constraints

* **Student Number Uniqueness**:
  - The system checks for existing student numbers ignoring case (`existsByStudentNumberIgnoreCase`).
  - Throws `BusinessRuleException("Student number already exists")` during creation or updates (excluding the current student's ID).

* **Email Address Uniqueness**:
  - If a student email address is supplied in the request, the system checks for case-insensitive duplicates across all other student contact records.
  - Throws `BusinessRuleException("Student email already exists")` during creation or updates.

* **Program and Curriculum Consistency**:
  - The student's academic program and curriculum are validated: `Curriculum` must belong to the chosen `Program`.
  - Throws `BusinessRuleException("Curriculum does not belong to the selected program")` if the `curriculum.program.id` does not match `program.id`.

* **Year Level Constraint**:
  - The database has a check constraint enforcing positive year levels: `year_level > 0`.
  - The DTO layer enforces this via the `@Min(1)` validation annotation on `yearLevel` in `StudentAcademicRequest`.

* **Cascade Deletes**:
  - Foreign keys on companion tables (`student_contacts`, `student_family_backgrounds`, `student_educational_backgrounds`, and `student_documents`) all specify `ON DELETE CASCADE` referencing `students(id)`. Deleting a student record will cascade delete all associated profiles and files in the database.

* **Audit Logging**:
  - The system automatically triggers structured audit logging using `AuditService` for all key write actions:
    - `STUDENT_CREATED`
    - `STUDENT_UPDATED`
    - `STUDENT_STATUS_UPDATED`
    - `DOCUMENT_UPLOADED`
    - `DOCUMENT_VERIFIED`
