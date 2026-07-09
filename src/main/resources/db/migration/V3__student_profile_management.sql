CREATE TABLE students (
    id UUID PRIMARY KEY,
    student_number VARCHAR(60) NOT NULL UNIQUE,
    first_name TEXT NOT NULL,
    middle_name TEXT,
    last_name TEXT NOT NULL,
    suffix TEXT,
    gender VARCHAR(20),
    birthdate DATE NOT NULL,
    birthplace TEXT,
    civil_status VARCHAR(40),
    nationality VARCHAR(80),
    religion VARCHAR(80),
    profile_photo_path TEXT,
    status VARCHAR(40) NOT NULL,
    program_id UUID NOT NULL REFERENCES programs(id),
    curriculum_id UUID NOT NULL REFERENCES curricula(id),
    year_level INTEGER NOT NULL,
    semester VARCHAR(40),
    section_id UUID REFERENCES sections(id),
    date_admitted DATE NOT NULL,
    school_year_admitted VARCHAR(20) NOT NULL,
    classification VARCHAR(40),
    academic_status VARCHAR(40),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT students_year_level_positive CHECK (year_level > 0)
);

CREATE INDEX idx_students_program_id ON students(program_id);
CREATE INDEX idx_students_curriculum_id ON students(curriculum_id);
CREATE INDEX idx_students_section_id ON students(section_id);
CREATE INDEX idx_students_status ON students(status);

CREATE TABLE student_contacts (
    student_id UUID PRIMARY KEY REFERENCES students(id) ON DELETE CASCADE,
    mobile_number VARCHAR(40),
    telephone_number VARCHAR(40),
    email_address VARCHAR(120) UNIQUE,
    current_address TEXT,
    permanent_address TEXT,
    province TEXT,
    city_municipality TEXT,
    barangay TEXT,
    zip_code VARCHAR(20),
    emergency_contact_name TEXT,
    emergency_contact_number VARCHAR(40),
    emergency_contact_relationship TEXT,
    emergency_contact_address TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE student_family_backgrounds (
    student_id UUID PRIMARY KEY REFERENCES students(id) ON DELETE CASCADE,
    father_name TEXT,
    father_occupation TEXT,
    father_contact_number VARCHAR(40),
    mother_name TEXT,
    mother_occupation TEXT,
    mother_contact_number VARCHAR(40),
    guardian_name TEXT,
    guardian_relationship TEXT,
    guardian_contact_number VARCHAR(40),
    guardian_address TEXT,
    household_income_range TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE student_educational_backgrounds (
    student_id UUID PRIMARY KEY REFERENCES students(id) ON DELETE CASCADE,
    elementary_school_name TEXT,
    elementary_school_address TEXT,
    elementary_year_graduated INTEGER,
    junior_high_school_name TEXT,
    junior_high_school_address TEXT,
    junior_high_school_year_graduated INTEGER,
    senior_high_school_name TEXT,
    senior_high_school_address TEXT,
    senior_high_school_strand TEXT,
    senior_high_school_year_graduated INTEGER,
    previous_college TEXT,
    previous_program TEXT,
    previous_school_year_attended TEXT,
    admission_type VARCHAR(40),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE student_documents (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(id) ON DELETE CASCADE,
    document_type VARCHAR(120) NOT NULL,
    file_name TEXT NOT NULL,
    file_path TEXT NOT NULL,
    mime_type VARCHAR(160),
    file_size BIGINT NOT NULL,
    uploaded_by UUID REFERENCES users(id),
    verification_status VARCHAR(40) NOT NULL,
    verified_by UUID REFERENCES users(id),
    verified_at TIMESTAMPTZ,
    remarks TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_student_documents_student_id ON student_documents(student_id);
CREATE INDEX idx_student_documents_status ON student_documents(verification_status);

INSERT INTO role_permissions (role_id, permission_id) VALUES
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000104'),
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000105'),
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000106'),
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000106'),
('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000106')
ON CONFLICT DO NOTHING;
