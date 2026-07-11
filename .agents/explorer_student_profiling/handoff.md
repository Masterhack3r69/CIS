# Handoff Report: Student Profiling Backend Exploration

## 1. Observation
- **Scope Directories**:
  - Entities: `c:\Users\PC\Projects\cis\src\main\java\com\school\sis\student\entity\*`
  - DTOs: `c:\Users\PC\Projects\cis\src\main\java\com\school\sis\student\dto\*`
  - Controller: `c:\Users\PC\Projects\cis\src\main\java\com\school\sis\student\controller\StudentController.java`
  - Service: `c:\Users\PC\Projects\cis\src\main\java\com\school\sis\student\service\StudentService.java`
  - Database Migration: `c:\Users\PC\Projects\cis\src\main\resources\db\migration\V3__student_profile_management.sql`
- **Verbatim Fields and Constraints**:
  - `Student` defines key columns such as `student_number` (VARCHAR(60), unique), `birthdate` (DATE), `status` (StudentStatus), `year_level` (int, check constraint `year_level > 0`), `program_id` (UUID), and `curriculum_id` (UUID).
  - Unique student number checking logic (Line 340-345 in `StudentService.java`):
    ```java
    private void validateUniqueStudentNumber(String studentNumber, UUID currentStudentId) {
        boolean exists = currentStudentId == null
                ? studentRepository.existsByStudentNumberIgnoreCase(studentNumber)
                : studentRepository.existsByStudentNumberIgnoreCaseAndIdNot(studentNumber, currentStudentId);
        if (exists) throw new BusinessRuleException("Student number already exists");
    }
    ```
  - Path traversal check on document upload (Line 168-170 in `StudentService.java`):
    ```java
    if (!target.startsWith(studentDirectory)) {
        throw new BusinessRuleException("Invalid file path");
    }
    ```

## 2. Logic Chain
- **Endpoint Structure**: By examining `StudentController.java` annotations (e.g., `@RestController`, `@RequestMapping("/api/v1/students")`), we mapped the 9 endpoints and their PreAuthorize security requirements (such as `STUDENT_VIEW`, `STUDENT_CREATE`, and `STUDENT_UPDATE`).
- **Data Layout and Validations**: By reviewing the DTOs (e.g. `StudentRequest`, `StudentPersonalRequest`, etc.) and matching them with `StudentService.java`'s `applyStudent` and `saveCompanionRecords` methods, we deduced the nullability, validation rules (e.g., `@NotBlank`, `@Min(1)`, and `@Email`), and DB mapping relationships.
- **Companion Entities**: We verified that `StudentContact`, `StudentFamilyBackground`, and `StudentEducationalBackground` use the `@MapsId` annotation in Hibernate and `PRIMARY KEY REFERENCES students(id) ON DELETE CASCADE` in SQL, sharing the exact same primary key as `Student` (the student's UUID).

## 3. Caveats
- No caveats. The database schema has been verified directly against the SQL migrations, and the controllers, DTOs, and services have been examined.

## 4. Conclusion
- The backend for the Student Profiling module is fully defined, secured under proper authorities (`STUDENT_VIEW`, `STUDENT_CREATE`, `STUDENT_UPDATE`), and maps cleanly to PostgreSQL with cascading deletes and uniqueness constraints. The findings have been fully documented in `analysis.md`.

## 5. Verification Method
- Execute the test suite via the Maven project test runner to ensure compiling and testing consistency:
  ```powershell
  mvn test
  ```
- File to inspect: `c:\Users\PC\Projects\cis\.agents\explorer_student_profiling\analysis.md`
