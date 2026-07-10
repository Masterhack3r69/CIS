ALTER TABLE users
    ADD COLUMN faculty_id UUID REFERENCES faculty(id);

CREATE INDEX idx_users_faculty_id ON users(faculty_id);

CREATE TABLE grades (
    id UUID PRIMARY KEY,
    enrollment_subject_id UUID NOT NULL UNIQUE REFERENCES enrollment_subjects(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES students(id),
    course_id UUID NOT NULL REFERENCES courses(id),
    section_id UUID NOT NULL REFERENCES sections(id),
    faculty_id UUID NOT NULL REFERENCES faculty(id),
    school_year_id UUID NOT NULL REFERENCES school_years(id),
    semester_id UUID NOT NULL REFERENCES semesters(id),
    final_grade NUMERIC(4, 2),
    remarks VARCHAR(40) NOT NULL DEFAULT 'NO_GRADE',
    status VARCHAR(40) NOT NULL DEFAULT 'DRAFT',
    encoded_by UUID REFERENCES users(id),
    encoded_at TIMESTAMPTZ,
    submitted_by UUID REFERENCES users(id),
    submitted_at TIMESTAMPTZ,
    approved_by UUID REFERENCES users(id),
    approved_at TIMESTAMPTZ,
    locked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT grades_final_grade_range CHECK (final_grade IS NULL OR (final_grade >= 1.00 AND final_grade <= 5.00))
);

CREATE INDEX idx_grades_student_id ON grades(student_id);
CREATE INDEX idx_grades_course_id ON grades(course_id);
CREATE INDEX idx_grades_section_id ON grades(section_id);
CREATE INDEX idx_grades_faculty_id ON grades(faculty_id);
CREATE INDEX idx_grades_term ON grades(school_year_id, semester_id);
CREATE INDEX idx_grades_status ON grades(status);

CREATE TABLE grade_status_history (
    id UUID PRIMARY KEY,
    grade_id UUID NOT NULL REFERENCES grades(id) ON DELETE CASCADE,
    from_status VARCHAR(40),
    to_status VARCHAR(40) NOT NULL,
    remarks TEXT,
    changed_by UUID REFERENCES users(id),
    changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_grade_status_history_grade_id ON grade_status_history(grade_id);

CREATE TABLE academic_records (
    id UUID PRIMARY KEY,
    grade_id UUID NOT NULL UNIQUE REFERENCES grades(id) ON DELETE CASCADE,
    student_id UUID NOT NULL REFERENCES students(id),
    program_id UUID NOT NULL REFERENCES programs(id),
    curriculum_id UUID NOT NULL REFERENCES curricula(id),
    course_id UUID NOT NULL REFERENCES courses(id),
    section_id UUID NOT NULL REFERENCES sections(id),
    faculty_id UUID NOT NULL REFERENCES faculty(id),
    school_year_id UUID NOT NULL REFERENCES school_years(id),
    semester_id UUID NOT NULL REFERENCES semesters(id),
    course_code VARCHAR(40) NOT NULL,
    course_title TEXT NOT NULL,
    credit_units NUMERIC(8, 2) NOT NULL,
    final_grade NUMERIC(4, 2),
    remarks VARCHAR(40) NOT NULL,
    grade_status VARCHAR(40) NOT NULL,
    earned_units NUMERIC(8, 2) NOT NULL DEFAULT 0,
    approved_at TIMESTAMPTZ,
    locked_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_academic_records_student_id ON academic_records(student_id);
CREATE INDEX idx_academic_records_course_id ON academic_records(course_id);
CREATE INDEX idx_academic_records_term ON academic_records(school_year_id, semester_id);
CREATE INDEX idx_academic_records_grade_status ON academic_records(grade_status);
