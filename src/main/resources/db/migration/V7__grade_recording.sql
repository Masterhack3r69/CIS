ALTER TABLE faculty
    ADD COLUMN user_id UUID REFERENCES users(id);

CREATE UNIQUE INDEX ux_faculty_user_id ON faculty(user_id) WHERE user_id IS NOT NULL;

CREATE TABLE grades (
    id UUID PRIMARY KEY,
    enrollment_subject_id UUID NOT NULL UNIQUE REFERENCES enrollment_subjects(id),
    student_id UUID NOT NULL REFERENCES students(id),
    class_schedule_id UUID NOT NULL REFERENCES class_schedules(id),
    course_id UUID NOT NULL REFERENCES courses(id),
    section_id UUID NOT NULL REFERENCES sections(id),
    faculty_id UUID NOT NULL REFERENCES faculty(id),
    school_year_id UUID NOT NULL REFERENCES school_years(id),
    semester_id UUID NOT NULL REFERENCES semesters(id),
    final_grade NUMERIC(4, 2),
    special_grade VARCHAR(20),
    remark VARCHAR(40) NOT NULL,
    status VARCHAR(40) NOT NULL,
    encoded_by UUID REFERENCES users(id),
    encoded_at TIMESTAMPTZ,
    submitted_by UUID REFERENCES users(id),
    submitted_at TIMESTAMPTZ,
    reviewed_by UUID REFERENCES users(id),
    reviewed_at TIMESTAMPTZ,
    approved_by UUID REFERENCES users(id),
    approved_at TIMESTAMPTZ,
    locked_at TIMESTAMPTZ,
    notes TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT grades_one_grade_value CHECK (
        (final_grade IS NOT NULL AND special_grade IS NULL)
        OR (final_grade IS NULL AND special_grade IS NOT NULL)
    ),
    CONSTRAINT grades_final_grade_range CHECK (
        final_grade IS NULL OR (final_grade >= 1.00 AND final_grade <= 5.00)
    )
);

CREATE INDEX idx_grades_student_id ON grades(student_id);
CREATE INDEX idx_grades_class_schedule_id ON grades(class_schedule_id);
CREATE INDEX idx_grades_faculty_id ON grades(faculty_id);
CREATE INDEX idx_grades_term ON grades(school_year_id, semester_id);
CREATE INDEX idx_grades_status ON grades(status);

CREATE TABLE grade_status_history (
    id UUID PRIMARY KEY,
    grade_id UUID NOT NULL REFERENCES grades(id) ON DELETE CASCADE,
    from_status VARCHAR(40),
    to_status VARCHAR(40) NOT NULL,
    changed_by UUID REFERENCES users(id),
    changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    remarks TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_grade_status_history_grade_id ON grade_status_history(grade_id);

CREATE TABLE academic_records (
    id UUID PRIMARY KEY,
    grade_id UUID NOT NULL UNIQUE REFERENCES grades(id),
    student_id UUID NOT NULL REFERENCES students(id),
    program_id UUID NOT NULL REFERENCES programs(id),
    curriculum_id UUID NOT NULL REFERENCES curricula(id),
    school_year_id UUID NOT NULL REFERENCES school_years(id),
    semester_id UUID NOT NULL REFERENCES semesters(id),
    course_id UUID NOT NULL REFERENCES courses(id),
    faculty_id UUID NOT NULL REFERENCES faculty(id),
    final_grade NUMERIC(4, 2),
    special_grade VARCHAR(20),
    remark VARCHAR(40) NOT NULL,
    grade_status VARCHAR(40) NOT NULL,
    credit_units NUMERIC(8, 2) NOT NULL,
    earned_units NUMERIC(8, 2) NOT NULL DEFAULT 0,
    approved_at TIMESTAMPTZ,
    locked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_academic_records_student_id ON academic_records(student_id);
CREATE INDEX idx_academic_records_term ON academic_records(school_year_id, semester_id);
CREATE INDEX idx_academic_records_course_id ON academic_records(course_id);

INSERT INTO permissions (id, name, description) VALUES
('00000000-0000-0000-0000-000000000120', 'GRADE_VIEW', 'Can view grades'),
('00000000-0000-0000-0000-000000000121', 'GRADE_REVIEW', 'Can review submitted grades')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id) VALUES
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000120'),
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000121'),
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000109'),
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000110'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000120'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000121'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000110'),
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000120'),
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000121'),
('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000120'),
('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000121'),
('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000120'),
('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000109'),
('00000000-0000-0000-0000-000000000208', '00000000-0000-0000-0000-000000000120')
ON CONFLICT DO NOTHING;
