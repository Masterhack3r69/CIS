CREATE TABLE enrollments (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(id),
    program_id UUID NOT NULL REFERENCES programs(id),
    section_id UUID REFERENCES sections(id),
    school_year_id UUID NOT NULL REFERENCES school_years(id),
    semester_id UUID NOT NULL REFERENCES semesters(id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    remarks TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_enrollments_student_id ON enrollments(student_id);
CREATE INDEX idx_enrollments_program_id ON enrollments(program_id);
CREATE INDEX idx_enrollments_section_id ON enrollments(section_id);
CREATE INDEX idx_enrollments_term ON enrollments(school_year_id, semester_id);
CREATE INDEX idx_enrollments_status ON enrollments(status);
CREATE UNIQUE INDEX ux_enrollments_active_term
    ON enrollments(student_id, school_year_id, semester_id)
    WHERE status <> 'CANCELLED';

CREATE TABLE enrollment_subjects (
    id UUID PRIMARY KEY,
    enrollment_id UUID NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    class_schedule_id UUID NOT NULL REFERENCES class_schedules(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ENROLLED',
    dropped_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_enrollment_subjects_enrollment_id ON enrollment_subjects(enrollment_id);
CREATE INDEX idx_enrollment_subjects_schedule_id ON enrollment_subjects(class_schedule_id);
CREATE INDEX idx_enrollment_subjects_status ON enrollment_subjects(status);
CREATE UNIQUE INDEX ux_enrollment_subjects_active_schedule
    ON enrollment_subjects(enrollment_id, class_schedule_id)
    WHERE status = 'ENROLLED';

CREATE TABLE enrollment_status_history (
    id UUID PRIMARY KEY,
    enrollment_id UUID NOT NULL REFERENCES enrollments(id) ON DELETE CASCADE,
    from_status VARCHAR(20),
    to_status VARCHAR(20) NOT NULL,
    remarks TEXT,
    changed_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_enrollment_status_history_enrollment_id ON enrollment_status_history(enrollment_id);

INSERT INTO permissions (id, name, description) VALUES
('00000000-0000-0000-0000-000000000118', 'ENROLLMENT_VIEW', 'Can view enrollment records')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id) VALUES
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000118'),
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000107'),
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000108'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000118'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000107'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000108'),
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000118'),
('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000118'),
('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000118')
ON CONFLICT DO NOTHING;
