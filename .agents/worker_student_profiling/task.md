# Task: Implement Student Profiling Frontend Workflows

## Objective
Implement complete React frontend workflows for the Student Profiling module in the `frontend` project. It must integrate fully with the backend REST API endpoints, validate inputs according to backend constraints, build without any TypeScript compilation errors, and render without runtime crashes.

## Requirements

### 1. Types Definition (`frontend/src/lib/types.ts`)
Add all necessary types for the Student Profiling module matching backend Java fields. Make sure to define:
- `Gender` enum (`MALE`, `FEMALE`, `OTHER`)
- `StudentStatus` enum (`APPLICANT`, `ACTIVE`, `ENROLLED`, `INACTIVE`, `DROPPED`, `TRANSFERRED`, `GRADUATED`, `ARCHIVED`)
- `StudentClassification` enum (`REGULAR`, `IRREGULAR`, `TRANSFEREE`, `RETURNEE`, `CROSS_ENROLLEE`, `GRADUATING`)
- `AcademicStatus` enum (`REGULAR`, `IRREGULAR`, `PROBATION`, `CANDIDATE_FOR_GRADUATION`, `GRADUATED`, `DISMISSED`, `ON_LEAVE`)
- `AdmissionType` enum (`NEW_STUDENT`, `TRANSFEREE`, `RETURNEE`, `SHIFTEE`, `CROSS_ENROLLEE`, `CONTINUING_STUDENT`)
- `DocumentVerificationStatus` enum (`PENDING`, `SUBMITTED`, `VERIFIED`, `REJECTED`, `MISSING`)
- `StudentPersonalRequest` and `StudentPersonalResponse`
- `StudentContactRequest` and `StudentContactResponse`
- `StudentFamilyRequest` and `StudentFamilyResponse`
- `StudentEducationalRequest` and `StudentEducationalResponse`
- `StudentAcademicRequest` and `StudentAcademicResponse`
- `StudentRequest` and `StudentResponse`
- `StudentSummary` / `StudentSummaryResponse`
- `StudentDocumentResponse`
- `DocumentVerificationRequest`
- `StudentAcademicRecordsResponse`

### 2. React Query Hooks (`frontend/src/hooks/use-students.ts`)
Create a new file `use-students.ts` under `frontend/src/hooks/` containing:
- `useStudents(criteria: { search?: string; programId?: string; yearLevel?: number; sectionId?: string; status?: string; schoolYearAdmitted?: string; documentStatus?: string }, page?: number, size?: number)`
- `useStudent(id: string)`
- `useCreateStudent()`
- `useUpdateStudent()`
- `useUpdateStudentStatus()`
- `useUploadStudentDocument(studentId: string)`
- `useStudentDocuments(studentId: string)`
- `useVerifyStudentDocument(studentId: string)`
- `useStudentAcademicRecords(studentId: string)`

### 3. Student List & Create Forms (`frontend/src/pages/students-page.tsx`)
Update `StudentsPage` component in `students-page.tsx`:
- Render a searchable data table to display all students.
- Search input matching Name, Student Number, or Email (maps to `search` query param).
- Filters for Program (using a select populated by programs from `usePrograms`), Year Level (numeric), and Status (dropdown of `StudentStatus` values).
- Pagination support (Previous, Next, page count).
- A "New Student" button that opens a dialog to create a student.
- Create form should collect all 5 sections:
  - Personal Details: Student Number, First/Middle/Last/Suffix, Gender, Birthdate (date picker/input), Birthplace, Civil Status, Nationality, Religion, status.
  - Academic Details: Program, Curriculum, Year Level, Semester, Section, Date Admitted, School Year Admitted, Classification, Academic Status. (Curriculum list should filter based on selected Program!).
  - Contact Details: Mobile, Telephone, Email, Current/Permanent Address, Province, City/Municipality, Barangay, Zip Code, Emergency Contact Details.
  - Family Details: Father, Mother, and Guardian details, Household Income.
  - Educational Background: Elementary, Junior/Senior High School details, Admission Type.
- Forms must have validations: Student Number, First/Last Name, Birthdate, Program, Curriculum, Year Level, Date Admitted, School Year Admitted are required. Email must be valid syntax. Use `react-hook-form` + `zod`.

### 4. Tabbed Student Profile View & Updates (`frontend/src/pages/students-page.tsx`)
Update `StudentDetailPage` component in `students-page.tsx`:
- Detailed layout with breadcrumb / Back button.
- Displays full name, student number, program, year level at the top.
- "Profile PDF" and "Curriculum" download buttons (pointing to the existing backend report endpoints `/reports/students/{id}/profile` and `/reports/students/{id}/curriculum-checklist`).
- Tabbed interface with 5 tabs: Personal, Contact, Family, Education, Documents.
- Each detail tab (Personal, Contact, Family, Education) must display the current details AND have an "Edit" button that opens a form (modal or inline edit mode) to modify and save that section's details using `useUpdateStudent`.
- Update API expects the entire `StudentRequest` DTO, so when saving a section, compile the current student data for other sections and send the updated section data along with it.

### 5. Document Management Tab (`frontend/src/pages/students-page.tsx`)
Inside the "Documents" tab of `StudentDetailPage`:
- Show the list of uploaded documents: Document Type, File Name, Size (formatted), Upload Date, Status (Badge indicating SUBMITTED, VERIFIED, REJECTED, etc.), Uploaded By, Remarks.
- An "Upload Document" button that opens a modal allowing selection of document type (e.g. Birth Certificate, Form 137, Transcript of Records, etc.) and file input. Uses multipart form data.
- A "Verify" button next to each document. Clicking it opens a small dialog to change the verification status (VERIFIED/REJECTED/PENDING/MISSING) and add remarks.

### 6. Build and Verification
- Run `npm run tsc` to verify zero TypeScript errors.
- Run `npm run build` to verify successful production compile.

**MANDATORY INTEGRITY WARNING**:
DO NOT CHEAT. All implementations must be genuine. DO NOT hardcode test results, create dummy/facade implementations, or circumvent the intended task. A Forensic Auditor will independently verify your work. Integrity violations WILL be detected and your work WILL be rejected.
