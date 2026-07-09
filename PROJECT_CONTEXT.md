# Student Information System (SIS) — Full Project Context

## 1. Project Overview

This project is a **web-based Student Information System (SIS)** for a college with approximately **4,000 students**.

The system will support core registrar and academic operations such as:

- Student profiling
- Enrollment management
- Course and curriculum management
- Department and program management
- Faculty management
- Section and class schedule management
- Fee setup and student assessment
- Grade recording
- Academic records management
- Printable reports and PDF generation

The system will be designed for internal school operations and may later support student and faculty portals.

The first version should focus on a reliable, maintainable, and scalable **MVP** that can run in a local or hosted environment.

---

## 2. Target Users

The system will be used by different college personnel and users depending on their assigned roles.

### 2.1 Super Admin

The Super Admin manages the whole system configuration.

Responsibilities:

- Manage users and roles
- Configure permissions
- Manage school years and semesters
- Manage departments, programs, and system settings
- View audit logs
- Maintain system-wide master data

### 2.2 Registrar

The Registrar manages student records and enrollment.

Responsibilities:

- Create and update student profiles
- Manage student academic records
- Process student enrollment
- Validate curriculum and prerequisites
- Approve or lock grades
- Generate student reports
- Print certificates, grade slips, and academic records

### 2.3 Department Dean / Program Head

The Dean or Program Head manages academic offerings under a department or program.

Responsibilities:

- View students under the department
- Review curriculum
- Monitor faculty loads
- Review schedules
- Review submitted grades if required by workflow

### 2.4 Faculty

Faculty members handle teaching-related functions.

Responsibilities:

- View assigned class schedules
- View class lists
- Encode grades
- Submit grades for approval
- View assigned subjects and sections

### 2.5 Cashier / Finance Staff

The Cashier or Finance Staff manages fees and student assessments.

Responsibilities:

- Configure school fees, laboratory fees, and miscellaneous fees
- Generate assessment records
- View student balances
- Record payment status, if payment module is included
- Print assessment forms or statements of account

### 2.6 Student

The Student role may be added in a later phase.

Possible responsibilities:

- View profile
- View enrolled subjects
- View class schedule
- View grades
- View assessment or balance
- Download student documents

---

## 3. Technology Stack

### 3.1 Backend Layer

| Layer | Technology |
|---|---|
| Language | Java 21 OpenJDK LTS |
| Framework | Spring Boot 3.x |
| Build Tool | Maven |
| Web Layer | Spring Web REST API |
| ORM | Spring Data JPA with Hibernate |
| Database Migration | Flyway |
| Database | PostgreSQL 16 |
| Cache / Sessions | Redis |
| Authentication | Spring Security with JWT |
| Background Jobs | Spring @Async and Quartz Scheduler |
| PDF Generation | Apache PDFBox |
| Email | Spring Mail with SMTP |
| Containerization | Docker and Docker Compose |
| Reverse Proxy | Nginx |

### 3.2 Suggested Frontend Layer

The frontend may be built with:

| Layer | Suggested Technology |
|---|---|
| Framework | React with TypeScript |
| Build Tool | Vite |
| Styling | Tailwind CSS |
| UI Components | shadcn/ui or custom components |
| API Calls | TanStack Query / Axios / Fetch |
| Forms | React Hook Form |
| Validation | Zod |
| Routing | React Router |
| State Management | Context, Zustand, or Redux Toolkit if needed |

---

## 4. System Scope

### 4.1 Included in MVP

The MVP should include:

1. Authentication and role-based access control
2. Student profiling
3. Faculty management
4. Department management
5. Program management
6. Course / subject management
7. Curriculum management
8. Section and schedule management
9. Enrollment processing
10. Fee setup and assessment
11. Grade recording
12. Academic record viewing
13. Basic PDF reports
14. Audit logs

### 4.2 Not Required in MVP

The following can be added in later phases:

- Online payment gateway
- Student self-enrollment
- Parent portal
- SMS notifications
- Advanced scheduling automation
- Advanced analytics dashboard
- Mobile app
- Learning Management System features
- Biometric attendance integration
- Library system integration
- HR and payroll integration

---

## 5. High-Level System Architecture

The system follows a layered architecture.

```text
Browser / Frontend Client
        |
        v
Nginx Reverse Proxy
        |
        v
Spring Boot REST API
        |
        +--------------------+
        |                    |
        v                    v
PostgreSQL 16              Redis
Main Database              Cache / Token / Session Support
        |
        v
Flyway Database Migrations
```

### 5.1 Backend Architecture

Suggested backend package structure:

```text
src/main/java/com/school/sis
├── auth
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   ├── service
│   └── security
├── student
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
├── faculty
├── department
├── program
├── course
├── curriculum
├── schedule
├── enrollment
├── fee
├── grade
├── academicrecord
├── report
├── audit
├── common
│   ├── config
│   ├── exception
│   ├── response
│   ├── pagination
│   └── util
└── SisApplication.java
```

### 5.2 Recommended Backend Pattern

Each module should follow this structure:

```text
Controller → Service → Repository → Entity
```

Responsibilities:

- Controller handles HTTP requests and responses.
- Service contains business logic.
- Repository handles database queries.
- Entity represents database tables.
- DTOs are used for request and response objects.
- Mappers convert entities to DTOs and DTOs to entities.

---

## 6. Authentication and Authorization

### 6.1 Authentication

The system should use:

- Spring Security
- JWT access token
- Refresh token
- Password hashing using BCrypt

Login flow:

1. User submits username/email and password.
2. Backend validates credentials.
3. Backend returns JWT access token and refresh token.
4. Frontend stores token securely.
5. Every protected API request sends the access token.
6. Backend validates the token and checks user permissions.

### 6.2 Roles

Default roles:

- SUPER_ADMIN
- REGISTRAR
- DEAN
- PROGRAM_HEAD
- FACULTY
- CASHIER
- STUDENT
- READ_ONLY_STAFF

### 6.3 Permission Examples

| Permission | Description |
|---|---|
| STUDENT_CREATE | Can create student profiles |
| STUDENT_UPDATE | Can update student profiles |
| STUDENT_VIEW | Can view student records |
| ENROLLMENT_CREATE | Can create enrollment records |
| ENROLLMENT_APPROVE | Can approve enrollment |
| GRADE_ENCODE | Can encode grades |
| GRADE_APPROVE | Can approve and lock grades |
| FEE_MANAGE | Can manage fee setup |
| REPORT_GENERATE | Can generate reports |
| USER_MANAGE | Can manage system users |

### 6.4 Security Rules

- Users must only access modules allowed by their role.
- Faculty should only see their assigned classes.
- Students should only see their own records.
- Registrar can access all student academic records.
- Cashier can access financial and assessment data but should not modify grades.
- Locked grades cannot be modified except by authorized users.
- All sensitive operations must be logged.

---

## 7. Core Modules

---

# 7.1 Student Profiling Module

## Purpose

The Student Profiling Module stores complete student information from admission until graduation, transfer, or withdrawal.

## Main Features

- Create student profile
- Update student personal information
- Manage contact information
- Manage family background
- Manage educational background
- Assign department, program, year level, and curriculum
- Upload and track student documents
- Search and filter students
- View student profile summary

## Student Personal Information

Fields:

- Student ID
- Student Number
- First Name
- Middle Name
- Last Name
- Suffix
- Full Name
- Gender
- Birthdate
- Birthplace
- Civil Status
- Nationality
- Religion
- Profile Photo
- Student Status

Possible student statuses:

- Applicant
- Active
- Enrolled
- Inactive
- Dropped
- Transferred
- Graduated
- Archived

## Contact Information

Fields:

- Mobile Number
- Telephone Number
- Email Address
- Current Address
- Permanent Address
- Province
- City / Municipality
- Barangay
- ZIP Code
- Emergency Contact Name
- Emergency Contact Number
- Emergency Contact Relationship
- Emergency Contact Address

## Family Background

Fields:

- Father Name
- Father Occupation
- Father Contact Number
- Mother Name
- Mother Occupation
- Mother Contact Number
- Guardian Name
- Guardian Relationship
- Guardian Contact Number
- Guardian Address
- Household Income Range, optional

## Educational Background

Fields:

- Elementary School Name
- Elementary School Address
- Elementary Year Graduated
- Junior High School Name
- Junior High School Address
- Junior High School Year Graduated
- Senior High School Name
- Senior High School Address
- Senior High School Strand
- Senior High School Year Graduated
- Previous College, for transferees
- Previous Program, for transferees
- Previous School Year Attended
- Admission Type

Admission types:

- New Student
- Transferee
- Returnee
- Shiftee
- Cross-Enrollee
- Continuing Student

## Academic Information

Fields:

- Department
- Program
- Curriculum Version
- Year Level
- Semester
- Section
- Date Admitted
- School Year Admitted
- Student Classification
- Academic Status

Student classifications:

- Regular
- Irregular
- Transferee
- Returnee
- Cross-Enrollee
- Graduating

## Student Documents

The system should allow document upload and verification.

Possible documents:

- Birth Certificate
- Form 138
- Form 137
- Good Moral Certificate
- Transcript of Records
- Certificate of Transfer Credential
- Honorable Dismissal
- ID Picture
- Medical Certificate
- Marriage Certificate, if applicable
- Other Requirements

Document fields:

- Document ID
- Student ID
- Document Type
- File Name
- File Path
- MIME Type
- File Size
- Upload Date
- Uploaded By
- Verification Status
- Verified By
- Verified At
- Remarks

Verification statuses:

- Pending
- Submitted
- Verified
- Rejected
- Missing

## Student Module Business Rules

1. Student number must be unique.
2. Student email must be unique if provided.
3. A student can only have one active program at a time unless the school supports dual programs.
4. A student must be assigned to a curriculum before enrollment.
5. Student profile cannot be deleted if the student already has enrollment or grade records.
6. Important student profile changes must be recorded in audit logs.

---

# 7.2 Faculty Module

## Purpose

The Faculty Module manages instructors and teachers who are assigned to class schedules and grade encoding.

## Main Features

- Create faculty profile
- Update faculty details
- Assign faculty to department
- Assign faculty to course schedules
- View faculty teaching load
- Prevent schedule conflicts
- Search faculty by name, department, or employment status

## Faculty Fields

- Faculty ID
- Employee Number
- First Name
- Middle Name
- Last Name
- Suffix
- Email
- Contact Number
- Department
- Employment Status
- Faculty Type
- Specialization
- Active Status

Employment statuses:

- Full-Time
- Part-Time
- Contractual
- Visiting Lecturer
- Inactive

Faculty types:

- Instructor
- Professor
- Lecturer
- Dean
- Program Head

## Faculty Business Rules

1. Employee number must be unique.
2. Faculty email must be unique.
3. A faculty member cannot be assigned to two schedules with overlapping times.
4. A faculty member can belong to one main department.
5. A faculty member may teach courses from other departments if allowed.
6. Inactive faculty cannot be assigned to new schedules.

---

# 7.3 Department Module

## Purpose

The Department Module manages academic departments or colleges.

## Department Fields

- Department ID
- Department Code
- Department Name
- Dean
- Description
- Active Status

Example:

| Department Code | Department Name | Dean |
|---|---|---|
| CCS | College of Computer Studies | Dr. Sample Dean |
| CTE | College of Teacher Education | Dr. Sample Dean |
| CAS | College of Arts and Sciences | Dr. Sample Dean |

## Department Business Rules

1. Department code must be unique.
2. Department name must be unique.
3. A department can have many programs.
4. A department can have many faculty members.
5. A department cannot be deleted if programs, faculty, or courses are linked to it.
6. Only one active dean should be assigned per department.

---

# 7.4 Program Module

## Purpose

The Program Module manages degree programs offered by the college.

## Program Fields

- Program ID
- Program Code
- Program Name
- Department
- Degree Type
- Program Duration
- Description
- Active Status

Example:

| Program Code | Program Name | Department |
|---|---|---|
| BSIT | Bachelor of Science in Information Technology | CCS |
| BSCS | Bachelor of Science in Computer Science | CCS |
| BSED | Bachelor of Secondary Education | CTE |
| BEED | Bachelor of Elementary Education | CTE |

## Degree Types

- Bachelor
- Associate
- Diploma
- Certificate
- Graduate Program

## Program Business Rules

1. Program code must be unique.
2. Program belongs to one department.
3. A program can have many curriculum versions.
4. A program can have many students.
5. A program cannot be deleted if students or curriculum records are linked to it.
6. Inactive programs cannot accept new student enrollment.

---

# 7.5 Course / Subject Module

## Purpose

The Course Module manages subjects used in curriculum and schedules.

A course represents a subject such as:

- CCS 1001 — Introduction to Computing
- CCS 1400 — Fundamentals of Programming
- GE 114 — Purposive Communication
- GE 115 — Ethics
- PE 1a — Physical Fitness and Wellness
- NSTP 1 — Civic Welfare Training Service

## Course Fields

- Course ID
- Course Code
- Course Title
- Course Description
- Lecture Hours Per Week
- Laboratory Hours Per Week
- Credit Units
- Course Type
- Department
- Active Status

## Course Types

- Major
- Professional Course
- General Education
- Physical Education
- NSTP
- Elective
- Laboratory
- Seminar
- Thesis / Capstone

## Example Course Records

| Course Code | Course Title | Lec Hrs/Week | Lab Hrs/Week | Credit Units |
|---|---|---:|---:|---:|
| CCS 1001 | Introduction to Computing | 2 | 3 | 3 |
| CCS 1400 | Fundamentals of Programming | 2 | 3 | 3 |
| GEMath 1 | Mathematics in the Modern World | 3 | 0 | 3 |
| GESocSci 1 | Understanding the Self | 3 | 0 | 3 |
| CETech 1 | Living in the IT Era | 3 | 0 | 3 |
| GE 114 | Purposive Communication | 3 | 0 | 3 |
| GE 115 | Ethics | 3 | 0 | 3 |
| GE 116 | Reading in Philippine History | 3 | 0 | 3 |
| PE 1a | Physical Fitness and Wellness | 2 | 0 | 2 |
| NSTP 1 | Civic Welfare Training Service | 3 | 0 | 3 |

## Course Business Rules

1. Course code must be unique.
2. Course title should be unique within the department if possible.
3. Credit units cannot be negative.
4. Lecture and laboratory hours cannot be negative.
5. Courses used in curriculum cannot be deleted.
6. Courses may be deactivated instead of deleted.
7. Courses can be reused across multiple curricula.

---

# 7.6 Curriculum Management Module

## Purpose

The Curriculum Module manages the official list of courses required for each program, grouped by year level and semester.

This module is based on the curriculum format shown in the provided image.

The curriculum should display:

- Grade column, optional for checklist view
- Course Code
- Course Title
- Lecture Hours per Week
- Laboratory Hours per Week
- Credit Units
- Prerequisites
- Co-requisites

## Curriculum Structure

A curriculum belongs to a program and has many curriculum courses.

Example:

```text
Program: Bachelor of Science in Information Technology
Curriculum Version: BSIT Curriculum 2026
Effective School Year: 2026-2027
Year Level: First Year
Semester: First Semester
```

## Curriculum Fields

- Curriculum ID
- Program ID
- Curriculum Code
- Curriculum Name
- Effective School Year
- Version
- Status
- Description

Curriculum statuses:

- Draft
- Active
- Inactive
- Archived

## Curriculum Course Fields

- Curriculum Course ID
- Curriculum ID
- Year Level
- Semester
- Course ID
- Course Code
- Course Title
- Lecture Hours Per Week
- Laboratory Hours Per Week
- Credit Units
- Sort Order
- Prerequisite Courses
- Co-requisite Courses
- Required Status

Required statuses:

- Required
- Optional
- Elective

## Example Curriculum: First Year, First Semester

| Course Code | Course Title | Lec Hrs/Week | Lab Hrs/Week | Credit Units | Prerequisites |
|---|---|---:|---:|---:|---|
| CCS 1001 | Introduction to Computing | 2 | 3 | 3 | None |
| CCS 1400 | Fundamentals of Programming | 2 | 3 | 3 | None |
| GEMath 1 | Mathematics in the Modern World | 3 | 0 | 3 | None |
| GESocSci 1 | Understanding the Self | 3 | 0 | 3 | None |
| CETech 1 | Living in the IT Era | 3 | 0 | 3 | None |
| Fil 12 | Sayko-Sosyolinggwistik na Pag-aaral ng Wikang Filipino | 3 | 0 | 3 | None |
| SEAL 1 | Student Enhancement Activities for Life I | 1 | 0 | 1 | None |
| RE 1 | Christianity in a Changing Society | 3 | 0 | 3 | None |
| PE 1a | Physical Fitness and Wellness | 2 | 0 | 2 | None |
| NSTP 1 | Civic Welfare Training Service | 3 | 0 | 3 | None |

Total academic units example:

```text
Lecture Hours: 25
Laboratory Hours: 6
Credit Units: 27
```

## Example Curriculum: First Year, Second Semester

| Course Code | Course Title | Lec Hrs/Week | Lab Hrs/Week | Credit Units | Prerequisites |
|---|---|---:|---:|---:|---|
| CCS 1500 | Intermediate Programming | 2 | 3 | 3 | CCS 1001, CCS 1400 |
| CCS 1301 | Data Structures and Algorithms | 2 | 3 | 3 | CCS 1400 |
| Fil 13 | Pagsusuri at Pagpapahalaga ng mga Kontemporaryong Panitikan | 3 | 0 | 3 | Fil 12 |
| SEAL 2 | Student Enhancement Activities for Life II | 1 | 0 | 1 | None |
| GEEng 1 | Purposive Communication | 3 | 0 | 3 | None |
| GESocSci 5 | Ethics | 3 | 0 | 3 | None |
| GESocSci 4 | The Entrepreneurial Mind | 3 | 0 | 3 | None |
| RE 2 | Christian Ethics in a Changing World | 3 | 0 | 3 | RE 1 |
| PE 1a | Physical Fitness and Wellness | 2 | 0 | 2 | None |
| NSTP 2 | Civic Welfare Training Service | 3 | 0 | 3 | NSTP 1 |

Total academic units example:

```text
Lecture Hours: 25
Laboratory Hours: 6
Credit Units: 27
```

## Curriculum Business Rules

1. A program can have multiple curriculum versions.
2. Only one curriculum version should be active for new students at a time, unless the school allows multiple active versions.
3. A curriculum course must belong to a valid course.
4. A curriculum course must have assigned year level and semester.
5. Prerequisite courses must exist in the curriculum or course catalog.
6. A student should follow the curriculum version assigned during admission.
7. A curriculum cannot be deleted if students are assigned to it.
8. Curriculum changes must be audited.
9. Total units should be automatically computed per semester and per year level.

---

# 7.7 Section and Schedule Module

## Purpose

The Section and Schedule Module manages class sections, class offerings, rooms, time slots, faculty assignments, and subject schedules.

This module is based on the schedule format shown in the provided image.

## Section Fields

- Section ID
- Section Code
- Program
- Year Level
- Semester
- School Year
- Maximum Capacity
- Current Enrolled Count
- Adviser, optional
- Active Status

Example section codes:

- BSIT-1A
- BSIT-1B
- BSCS-2A
- BSED-3A

## Room Fields

- Room ID
- Room Code
- Room Name
- Building
- Capacity
- Room Type
- Active Status

Room types:

- Lecture Room
- Laboratory Room
- Computer Laboratory
- Audio Visual Room
- Gym
- Online

## Schedule Fields

- Schedule ID
- School Year
- Semester
- Section
- Course
- Faculty
- Room
- Day Pattern
- Start Time
- End Time
- Schedule Type
- Capacity
- Active Status

Schedule types:

- Lecture
- Laboratory
- Combined
- Online
- Hybrid

## Day Patterns

The system should support common day patterns:

- M
- T
- W
- TH
- F
- S
- MW
- TTH
- MWF

## Example Schedule Records

| Subject Code | Description | Day | Time | Room | Subject Teacher |
|---|---|---|---|---|---|
| GE 114 | Purposive Communication | TTH | 1:00 PM - 2:30 PM | ALB 2 | Dr. Mary Grace Z. Agbas |
| GE 115 | Ethics | TTH | 2:30 PM - 4:00 PM | ALB 12 | Jether Yares |
| GE 116 | Reading in Philippine History | MW | 10:30 AM - 12:00 PM | ALB 5 | Sapong Jeremiah |
| CD 121 | Philippine Society and Community Development | TTH | 9:00 AM - 10:30 AM | ALB 2 | Bernadeth A. Talip |
| CD 122 | Technical Writing | MW | 2:30 PM - 4:00 PM | ALB 3 | Cherry Ann P. Cutad |
| PE 122 | Fitness Exercise | Mon | 8:00 AM - 10:00 AM | ALB 2 | Minette Cifra |
| NSTP 122 | National Service Training Program 2 | TTh | 7:30 AM - 9:00 AM | ALB 2 | E. Espejo |

## Schedule Conflict Rules

The system must prevent:

1. Same teacher assigned to two classes at the same day and time.
2. Same room assigned to two classes at the same day and time.
3. Same section assigned to overlapping subjects.
4. End time earlier than start time.
5. Duplicate schedule entries.
6. Schedule assigned to inactive faculty.
7. Schedule assigned to inactive room.
8. Schedule assigned to inactive course.
9. Section exceeding maximum capacity.

## Time Overlap Logic

A schedule overlaps if:

```text
existing.startTime < new.endTime
AND
new.startTime < existing.endTime
AND
same day pattern intersects
```

Example:

```text
Existing: TTH 1:00 PM - 2:30 PM
New:      TTH 2:00 PM - 3:00 PM
Result:   Conflict
```

---

# 7.8 Enrollment Module

## Purpose

The Enrollment Module handles student registration per school year and semester.

It connects the following:

- Student
- Program
- Curriculum
- School Year
- Semester
- Section
- Subjects
- Fees
- Assessment
- Enrollment status

## Enrollment Flow

1. Search and select student.
2. Select school year and semester.
3. Confirm student program and curriculum.
4. Load curriculum subjects for the selected year level and semester.
5. Show available sections and schedules.
6. Validate prerequisites.
7. Validate schedule conflicts.
8. Select subjects for enrollment.
9. Compute total units.
10. Generate fee assessment.
11. Save enrollment as draft or confirm enrollment.
12. Mark enrollment status.

## Enrollment Statuses

- Draft
- Pending Assessment
- Assessed
- Enrolled
- Cancelled
- Dropped
- Completed

## Enrollment Fields

- Enrollment ID
- Student ID
- School Year
- Semester
- Program
- Curriculum
- Year Level
- Section
- Enrollment Date
- Enrollment Status
- Total Units
- Created By
- Approved By
- Approved At

## Enrollment Subject Fields

- Enrollment Subject ID
- Enrollment ID
- Course ID
- Schedule ID
- Section ID
- Faculty ID
- Units
- Subject Status
- Grade Status

Subject statuses:

- Enrolled
- Dropped
- Cancelled
- Completed

Grade statuses:

- No Grade
- Encoded
- Submitted
- Approved
- Locked

## Enrollment Business Rules

1. A student can only have one active enrollment per school year and semester.
2. A student must have an assigned program and curriculum before enrollment.
3. The system should suggest subjects based on the student's curriculum.
4. Students must pass prerequisite courses before enrolling in advanced courses.
5. Authorized users may override prerequisite validation with remarks.
6. The system should prevent duplicate subjects in one enrollment.
7. The system should prevent overlapping schedules for the same student.
8. Enrollment cannot be confirmed if fee assessment is not generated, if required by policy.
9. Cancelled enrollment should not appear as active enrollment.
10. Enrollment changes must be recorded in audit logs.

---

# 7.9 Fees and Assessment Module

## Purpose

The Fees Module manages fee setup and generates student assessment during enrollment.

The module should support:

- School fees
- Tuition fees
- Laboratory fees
- Miscellaneous fees
- Other fees

## Fee Categories

- Tuition Fee
- Laboratory Fee
- Miscellaneous Fee
- Registration Fee
- Library Fee
- Medical Fee
- ID Fee
- Athletic Fee
- Computer Fee
- Other Fee

## Fee Setup Fields

- Fee ID
- Fee Code
- Fee Name
- Fee Category
- Amount
- Computation Type
- Program
- Year Level
- Semester
- School Year
- Active Status

## Computation Types

- Fixed Amount
- Per Unit
- Per Subject
- Per Laboratory Subject
- Per Semester
- Per Program
- Per Year Level

## Example Fee Rules

| Fee Type | Computation | Example |
|---|---|---|
| Tuition Fee | Per Unit | Credit Units × Rate per Unit |
| Laboratory Fee | Per Laboratory Subject | Applied to subjects with lab hours |
| Miscellaneous Fee | Fixed Amount | Registration, library, medical |
| ID Fee | Fixed Amount | One-time or yearly |
| Computer Fee | Fixed Amount or Per Lab Subject | Applied to IT/computer subjects |

## Assessment Fields

- Assessment ID
- Student ID
- Enrollment ID
- School Year
- Semester
- Total Units
- Tuition Amount
- Laboratory Fee Amount
- Miscellaneous Fee Amount
- Other Fee Amount
- Discount Amount
- Penalty Amount
- Total Assessment
- Amount Paid
- Balance
- Assessment Status

Assessment statuses:

- Unpaid
- Partial
- Paid
- Cancelled
- Refunded

## Fee Business Rules

1. Fee setup should be school-year specific.
2. Fees may be program-specific or applicable to all programs.
3. Tuition can be computed per unit.
4. Laboratory fees can be applied based on laboratory hours or laboratory subject.
5. Miscellaneous fees can be fixed per semester.
6. Assessment should be generated after subject selection.
7. Assessment should update if enrollment subjects change before final confirmation.
8. Paid assessments should not be deleted.
9. Fee changes should not automatically change already finalized assessments unless explicitly recalculated by an authorized user.
10. Fee and assessment changes must be logged.

---

# 7.10 Grade Recording Module

## Purpose

The Grade Recording Module manages grade encoding, submission, approval, locking, and academic record integration.

## Grade Fields

- Grade ID
- Student ID
- Enrollment Subject ID
- Course ID
- Section ID
- Faculty ID
- School Year
- Semester
- Final Grade
- Grade Remarks
- Grade Status
- Encoded By
- Encoded At
- Submitted By
- Submitted At
- Approved By
- Approved At
- Locked At

## Grade Remarks

- Passed
- Failed
- Incomplete
- Dropped
- No Grade
- Withdrawn
- Conditional

## Grade Statuses

- Draft
- Encoded
- Submitted
- Reviewed
- Approved
- Locked
- Returned for Correction

## Grade Workflow

1. Faculty encodes grades for assigned classes.
2. Faculty saves grades as draft.
3. Faculty submits grades.
4. Department head or program head reviews grades, if required.
5. Registrar approves grades.
6. System locks grades.
7. Approved grades become part of the academic record.

## Grade Business Rules

1. Faculty can only encode grades for assigned schedules.
2. Registrar can view and approve all grades.
3. Grades cannot be changed after locking unless a correction process is started.
4. Invalid grade values should be rejected.
5. Failed grades should not satisfy prerequisites.
6. Incomplete grades should not satisfy prerequisites unless completed.
7. Dropped subjects should appear in academic records depending on school policy.
8. Grade changes must be audited.
9. The system should support grade correction requests in a later phase.

---

# 7.11 Academic Records Module

## Purpose

The Academic Records Module stores and displays the official academic history of a student.

This module is used by the Registrar to view student progress, grades, completed subjects, failed subjects, and remaining curriculum subjects.

## Main Features

- View student academic history
- View grades by semester
- View curriculum checklist
- Track passed and failed subjects
- Track remaining subjects
- Compute total earned units
- Generate grade slip
- Generate evaluation sheet
- Generate transcript-style records

## Academic Record Data

- Student
- Program
- Curriculum
- School Year
- Semester
- Course
- Course Title
- Units
- Grade
- Remarks
- Faculty
- Date Approved
- Grade Status

## Academic Standing

Possible academic standing values:

- Regular
- Irregular
- Probation
- Candidate for Graduation
- Graduated
- Dismissed
- On Leave

## Academic Records Business Rules

1. Only approved and locked grades should be considered official.
2. Failed subjects should appear in the academic record.
3. Retaken subjects should be tracked properly.
4. Curriculum progress should compare passed subjects against required curriculum courses.
5. Earned units should count passed subjects only.
6. GPA or weighted average should follow the school grading policy.
7. Registrar should be able to generate PDF reports.

---

# 7.12 Reports and PDF Module

## Purpose

The Reports Module generates printable documents for students, faculty, and administrators.

PDF generation should use Apache PDFBox.

## Reports for MVP

- Student profile report
- Student list report
- Enrollment form
- Assessment form
- Class list
- Grade sheet
- Grade slip
- Curriculum checklist
- Academic record summary
- Certificate of Enrollment

## Future Reports

- Transcript of Records
- Certificate of Grades
- Certificate of Good Moral
- Graduation evaluation
- Faculty loading report
- Room utilization report
- Enrollment statistics
- Financial collection report

## Report Business Rules

1. Reports should include school name and logo if configured.
2. Reports should include date generated.
3. Reports should include generated by user.
4. Official reports should include control number if required.
5. Sensitive reports should only be generated by authorized roles.
6. PDF generation should be logged.

---

# 7.13 Audit Log Module

## Purpose

The Audit Log Module records important system activities.

## Audit Events

The system should log:

- User login
- Failed login attempts
- Student profile creation
- Student profile update
- Document upload
- Document verification
- Course creation or update
- Curriculum creation or update
- Schedule creation or update
- Enrollment creation
- Enrollment confirmation
- Fee setup changes
- Assessment generation
- Grade encoding
- Grade submission
- Grade approval
- Grade correction
- User role changes

## Audit Log Fields

- Audit Log ID
- User ID
- Action
- Module
- Entity Type
- Entity ID
- Old Value
- New Value
- IP Address
- User Agent
- Timestamp

## Audit Business Rules

1. Audit logs should not be editable by normal users.
2. Audit logs should not be deleted through the normal UI.
3. Sensitive updates should include before and after values.
4. Audit logs should be searchable by module, user, date, and action.

---

## 8. Suggested Database Entities

### 8.1 Authentication Entities

- users
- roles
- permissions
- user_roles
- role_permissions
- refresh_tokens

### 8.2 Academic Setup Entities

- departments
- programs
- courses
- school_years
- semesters
- rooms
- sections

### 8.3 Student Entities

- students
- student_contacts
- student_family_backgrounds
- student_educational_backgrounds
- student_documents

### 8.4 Faculty Entities

- faculty
- faculty_departments
- faculty_loads, optional

### 8.5 Curriculum Entities

- curricula
- curriculum_courses
- course_prerequisites
- course_corequisites

### 8.6 Schedule Entities

- class_schedules
- schedule_meetings, optional if one class has multiple meeting patterns

### 8.7 Enrollment Entities

- enrollments
- enrollment_subjects
- enrollment_status_history

### 8.8 Fee Entities

- fee_items
- fee_rules
- assessments
- assessment_items
- payments, optional for MVP

### 8.9 Grade Entities

- grades
- grade_change_requests, future
- grade_status_history

### 8.10 Record and Report Entities

- academic_records
- generated_reports
- audit_logs

---

## 9. Important Relationships

```text
Department 1 ─── * Program
Department 1 ─── * Faculty
Department 1 ─── * Course

Program 1 ─── * Curriculum
Program 1 ─── * Student
Program 1 ─── * Section

Curriculum 1 ─── * CurriculumCourse
Course 1 ─── * CurriculumCourse

Student 1 ─── * Enrollment
Enrollment 1 ─── * EnrollmentSubject
EnrollmentSubject 1 ─── 1 Grade

Section 1 ─── * ClassSchedule
Course 1 ─── * ClassSchedule
Faculty 1 ─── * ClassSchedule
Room 1 ─── * ClassSchedule

Enrollment 1 ─── 1 Assessment
Assessment 1 ─── * AssessmentItem
```

---

## 10. Naming Conventions

### 10.1 Database Table Naming

Use snake_case plural table names.

Examples:

```text
students
student_contacts
departments
programs
courses
curricula
curriculum_courses
class_schedules
enrollments
enrollment_subjects
fee_items
assessments
grades
audit_logs
```

### 10.2 Java Naming

Use standard Java naming.

Examples:

```text
Student
StudentController
StudentService
StudentRepository
StudentRequest
StudentResponse
```

### 10.3 API Naming

Use RESTful endpoints.

Examples:

```text
/api/v1/students
/api/v1/faculty
/api/v1/departments
/api/v1/programs
/api/v1/courses
/api/v1/curricula
/api/v1/sections
/api/v1/schedules
/api/v1/enrollments
/api/v1/fees
/api/v1/grades
/api/v1/reports
```

---

## 11. Suggested REST API Endpoints

### 11.1 Auth API

```text
POST   /api/v1/auth/login
POST   /api/v1/auth/refresh
POST   /api/v1/auth/logout
GET    /api/v1/auth/me
```

### 11.2 Student API

```text
GET    /api/v1/students
POST   /api/v1/students
GET    /api/v1/students/{id}
PUT    /api/v1/students/{id}
PATCH  /api/v1/students/{id}/status
POST   /api/v1/students/{id}/documents
GET    /api/v1/students/{id}/documents
PATCH  /api/v1/students/{id}/documents/{documentId}/verify
GET    /api/v1/students/{id}/academic-records
```

### 11.3 Faculty API

```text
GET    /api/v1/faculty
POST   /api/v1/faculty
GET    /api/v1/faculty/{id}
PUT    /api/v1/faculty/{id}
GET    /api/v1/faculty/{id}/schedules
GET    /api/v1/faculty/{id}/loads
```

### 11.4 Department API

```text
GET    /api/v1/departments
POST   /api/v1/departments
GET    /api/v1/departments/{id}
PUT    /api/v1/departments/{id}
PATCH  /api/v1/departments/{id}/status
```

### 11.5 Program API

```text
GET    /api/v1/programs
POST   /api/v1/programs
GET    /api/v1/programs/{id}
PUT    /api/v1/programs/{id}
GET    /api/v1/programs/{id}/curricula
```

### 11.6 Course API

```text
GET    /api/v1/courses
POST   /api/v1/courses
GET    /api/v1/courses/{id}
PUT    /api/v1/courses/{id}
PATCH  /api/v1/courses/{id}/status
```

### 11.7 Curriculum API

```text
GET    /api/v1/curricula
POST   /api/v1/curricula
GET    /api/v1/curricula/{id}
PUT    /api/v1/curricula/{id}
POST   /api/v1/curricula/{id}/courses
PUT    /api/v1/curricula/{id}/courses/{curriculumCourseId}
DELETE /api/v1/curricula/{id}/courses/{curriculumCourseId}
GET    /api/v1/curricula/{id}/checklist
POST   /api/v1/curricula/{id}/activate
```

### 11.8 Section and Schedule API

```text
GET    /api/v1/sections
POST   /api/v1/sections
GET    /api/v1/sections/{id}
PUT    /api/v1/sections/{id}

GET    /api/v1/schedules
POST   /api/v1/schedules
GET    /api/v1/schedules/{id}
PUT    /api/v1/schedules/{id}
DELETE /api/v1/schedules/{id}
POST   /api/v1/schedules/check-conflict
```

### 11.9 Enrollment API

```text
GET    /api/v1/enrollments
POST   /api/v1/enrollments
GET    /api/v1/enrollments/{id}
PUT    /api/v1/enrollments/{id}
POST   /api/v1/enrollments/{id}/subjects
DELETE /api/v1/enrollments/{id}/subjects/{subjectId}
POST   /api/v1/enrollments/{id}/validate
POST   /api/v1/enrollments/{id}/generate-assessment
POST   /api/v1/enrollments/{id}/confirm
POST   /api/v1/enrollments/{id}/cancel
```

### 11.10 Fee API

```text
GET    /api/v1/fees
POST   /api/v1/fees
GET    /api/v1/fees/{id}
PUT    /api/v1/fees/{id}
PATCH  /api/v1/fees/{id}/status

GET    /api/v1/assessments
GET    /api/v1/assessments/{id}
POST   /api/v1/assessments/{id}/recalculate
PATCH  /api/v1/assessments/{id}/status
```

### 11.11 Grade API

```text
GET    /api/v1/grades
GET    /api/v1/grades/class/{scheduleId}
POST   /api/v1/grades/class/{scheduleId}/encode
POST   /api/v1/grades/class/{scheduleId}/submit
POST   /api/v1/grades/class/{scheduleId}/approve
POST   /api/v1/grades/class/{scheduleId}/lock
GET    /api/v1/grades/student/{studentId}
```

### 11.12 Report API

```text
GET    /api/v1/reports/students/{id}/profile
GET    /api/v1/reports/students/{id}/grade-slip
GET    /api/v1/reports/students/{id}/curriculum-checklist
GET    /api/v1/reports/enrollments/{id}/form
GET    /api/v1/reports/assessments/{id}
GET    /api/v1/reports/classes/{scheduleId}/class-list
GET    /api/v1/reports/classes/{scheduleId}/grade-sheet
```

---

## 12. Validation Rules

### Student Validation

- First name is required.
- Last name is required.
- Student number must be unique.
- Birthdate must be valid.
- Email format must be valid.
- Program is required before enrollment.
- Curriculum is required before enrollment.

### Course Validation

- Course code is required and unique.
- Course title is required.
- Credit units must be zero or greater.
- Lecture hours must be zero or greater.
- Laboratory hours must be zero or greater.

### Curriculum Validation

- Program is required.
- Curriculum version is required.
- Course must exist before adding to curriculum.
- Year level is required.
- Semester is required.
- Duplicate course in the same year and semester should be prevented.

### Schedule Validation

- Section is required.
- Course is required.
- Faculty is required.
- Room is required.
- Day pattern is required.
- Start time is required.
- End time is required.
- End time must be after start time.
- Room conflict must be checked.
- Faculty conflict must be checked.
- Section conflict must be checked.

### Enrollment Validation

- Student must exist.
- School year and semester must be active.
- Student must not have duplicate active enrollment.
- Selected subjects must belong to curriculum or be approved as irregular subjects.
- Prerequisites must be validated.
- Schedule conflicts must be checked.
- Total units must be computed.

### Grade Validation

- Grade must follow school grading format.
- Faculty can only encode grades for assigned classes.
- Locked grades cannot be edited.
- Approved grade must update academic records.

---

## 13. Non-Functional Requirements

### 13.1 Performance

The system should handle approximately 4,000 students.

Expected usage:

- Daily registrar operations
- Multiple staff users logged in at the same time
- Faculty grade encoding during grading period
- Student records search
- PDF report generation

Recommended performance goals:

- Most API responses should return within 300ms to 1s.
- Search endpoints should support pagination.
- Large reports should be generated asynchronously if needed.
- Frequently used lookup data may be cached in Redis.

### 13.2 Scalability

The system should support:

- More students in the future
- Additional programs and departments
- More modules such as payment, attendance, and portal features
- Deployment on a local server or VPS/cloud server

### 13.3 Reliability

The system should:

- Use database transactions for enrollment, assessment, and grade approval.
- Prevent duplicate records.
- Prevent schedule conflicts.
- Maintain audit logs for critical changes.
- Use Flyway migrations for database version control.
- Support database backups.

### 13.4 Security

The system should:

- Use HTTPS in production.
- Hash passwords with BCrypt.
- Use JWT with expiration.
- Use refresh tokens securely.
- Validate all inputs.
- Protect endpoints with role-based authorization.
- Prevent unauthorized record access.
- Log important actions.
- Avoid exposing sensitive errors.

### 13.5 Maintainability

The system should:

- Use modular package structure.
- Use DTOs instead of exposing entities directly.
- Use consistent naming conventions.
- Use Flyway for database migrations.
- Use service classes for business logic.
- Use centralized exception handling.
- Use automated tests for critical services.

---

## 14. Docker Deployment Context

The system should support Docker-based local development and production deployment.

### 14.1 Services

Docker Compose may include:

- backend
- frontend
- postgres
- redis
- nginx

Example service responsibilities:

```text
frontend  → serves React web app
backend   → Spring Boot REST API
postgres  → main database
redis     → cache/session/token support
nginx     → reverse proxy
```

### 14.2 Environment Variables

Backend environment variables:

```text
SPRING_PROFILES_ACTIVE=prod
DB_HOST=postgres
DB_PORT=5432
DB_NAME=sis_db
DB_USERNAME=sis_user
DB_PASSWORD=change_me
REDIS_HOST=redis
REDIS_PORT=6379
JWT_SECRET=change_me
JWT_ACCESS_EXPIRATION=900
JWT_REFRESH_EXPIRATION=604800
MAIL_HOST=smtp.example.com
MAIL_PORT=587
MAIL_USERNAME=
MAIL_PASSWORD=
```

---

## 15. Suggested MVP Development Phases

## Phase 1: Foundation

Deliverables:

- Spring Boot project setup
- PostgreSQL connection
- Flyway migration setup
- Docker Compose setup
- Global exception handling
- Base response format
- Authentication and JWT
- Role and permission model

## Phase 2: Academic Setup

Deliverables:

- Department CRUD
- Program CRUD
- Faculty CRUD
- Course CRUD
- School year and semester setup
- Room setup

## Phase 3: Curriculum Management

Deliverables:

- Curriculum CRUD
- Add courses to curriculum
- Group curriculum by year level and semester
- Add prerequisites and co-requisites
- Compute total units
- Activate curriculum version

## Phase 4: Student Profiling

Deliverables:

- Student profile CRUD
- Contact information
- Family background
- Educational background
- Academic information
- Document upload and verification

## Phase 5: Section and Schedule Management

Deliverables:

- Section CRUD
- Schedule CRUD
- Faculty assignment
- Room assignment
- Schedule conflict validation
- Section schedule view

## Phase 6: Enrollment

Deliverables:

- Create enrollment
- Load curriculum subjects
- Validate prerequisites
- Select schedules
- Validate schedule conflicts
- Confirm enrollment
- Enrollment status tracking

## Phase 7: Fees and Assessment

Deliverables:

- Fee setup
- Fee rules
- Assessment generation
- Assessment item breakdown
- Assessment PDF

## Phase 8: Grade Recording

Deliverables:

- Faculty class list
- Grade encoding
- Grade submission
- Registrar approval
- Grade locking
- Academic record update

## Phase 9: Reports

Deliverables:

- Enrollment form
- Student profile report
- Class list
- Grade sheet
- Grade slip
- Curriculum checklist
- Assessment form

## Phase 10: Polish and Production

Deliverables:

- Audit logs
- Dashboard
- Search filters
- Pagination
- Production Docker setup
- Nginx reverse proxy
- Backup plan
- Testing and documentation

---

## 16. Dashboard Requirements

### Super Admin Dashboard

Cards:

- Total students
- Total faculty
- Total programs
- Active school year
- Active semester
- Recent audit logs

### Registrar Dashboard

Cards:

- Active students
- Pending enrollments
- Enrolled students this semester
- Students with missing documents
- Recently updated student records

### Dean / Program Head Dashboard

Cards:

- Students per program
- Faculty under department
- Subjects offered
- Pending grade submissions
- Section schedules

### Faculty Dashboard

Cards:

- Assigned subjects
- Class schedules
- Total students handled
- Pending grade encoding
- Submitted grades

### Cashier Dashboard

Cards:

- Generated assessments
- Paid assessments
- Unpaid assessments
- Partial payments
- Total assessed amount

---

## 17. Search and Filtering Requirements

The system should support search and filters in major modules.

### Student Search Filters

- Student number
- Name
- Program
- Year level
- Section
- Student status
- School year admitted
- Document status

### Faculty Search Filters

- Employee number
- Name
- Department
- Employment status
- Active status

### Course Search Filters

- Course code
- Course title
- Department
- Course type
- Active status

### Schedule Search Filters

- School year
- Semester
- Program
- Section
- Faculty
- Room
- Day
- Course

### Enrollment Search Filters

- School year
- Semester
- Program
- Year level
- Section
- Enrollment status

### Grade Search Filters

- School year
- Semester
- Faculty
- Section
- Course
- Grade status

---

## 18. Standard API Response Format

Use a consistent API response format.

### Success Response

```json
{
  "success": true,
  "message": "Request successful",
  "data": {},
  "timestamp": "2026-01-01T08:00:00"
}
```

### Paginated Response

```json
{
  "success": true,
  "message": "Records retrieved successfully",
  "data": {
    "items": [],
    "page": 0,
    "size": 10,
    "totalElements": 100,
    "totalPages": 10
  },
  "timestamp": "2026-01-01T08:00:00"
}
```

### Error Response

```json
{
  "success": false,
  "message": "Validation failed",
  "errors": [
    {
      "field": "courseCode",
      "message": "Course code is required"
    }
  ],
  "timestamp": "2026-01-01T08:00:00"
}
```

---

## 19. Recommended Database Constraints

The database should enforce important constraints.

Examples:

```text
users.email UNIQUE
students.student_number UNIQUE
faculty.employee_number UNIQUE
departments.department_code UNIQUE
programs.program_code UNIQUE
courses.course_code UNIQUE
rooms.room_code UNIQUE
sections.section_code + school_year_id + semester_id UNIQUE
enrollments.student_id + school_year_id + semester_id UNIQUE
```

Use foreign keys for relationships.

Use soft delete or active status for important academic records instead of hard delete.

---

## 20. Data Retention and Backup

The system should preserve academic records permanently.

Backup recommendations:

- Daily database backup
- Weekly full backup
- Monthly archived backup
- Store backup outside the main server
- Test restore process regularly

Records that should not be hard deleted:

- Student profiles with academic history
- Enrollments
- Grades
- Assessments
- Payments
- Audit logs

---

## 21. Implementation Notes for AI Coding Agent

When generating code for this project:

1. Use Java 21 and Spring Boot 3.x.
2. Use Maven.
3. Use PostgreSQL 16.
4. Use Spring Data JPA.
5. Use Flyway migrations.
6. Use DTOs for requests and responses.
7. Do not expose JPA entities directly from controllers.
8. Use service classes for business logic.
9. Use repository interfaces for database access.
10. Use transaction boundaries for enrollment, assessment, and grade approval.
11. Use validation annotations for request DTOs.
12. Use global exception handling.
13. Use pagination for list endpoints.
14. Use role-based authorization with Spring Security.
15. Use JWT authentication.
16. Use Redis only where useful, not for every query.
17. Keep the MVP simple but structured.
18. Implement modules one phase at a time.
19. Write clean and maintainable code.
20. Add tests for important business rules.

---

## 22. Final Project Description

This project is a college-level Student Information System built with Java 21, Spring Boot 3.x, PostgreSQL 16, Redis, Docker, and Nginx.

The system manages student profiles, departments, programs, courses, curriculum, schedules, enrollment, fees, grades, and academic records.

It is designed for approximately 4,000 students and should support reliable registrar operations, curriculum-based enrollment, schedule validation, grade recording, fee assessment, and PDF report generation.

The MVP should prioritize correctness, clean architecture, secure role-based access, database integrity, and maintainability.
