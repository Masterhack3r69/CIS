CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE permissions (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE roles (
    id UUID PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    description TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE role_permissions (
    role_id UUID NOT NULL REFERENCES roles(id),
    permission_id UUID NOT NULL REFERENCES permissions(id),
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE users (
    id UUID PRIMARY KEY,
    email VARCHAR(120) NOT NULL UNIQUE,
    username VARCHAR(80) NOT NULL UNIQUE,
    password_hash TEXT NOT NULL,
    full_name TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id),
    role_id UUID NOT NULL REFERENCES roles(id),
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    token VARCHAR(160) NOT NULL UNIQUE,
    user_id UUID NOT NULL REFERENCES users(id),
    expires_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE departments (
    id UUID PRIMARY KEY,
    department_code VARCHAR(40) NOT NULL UNIQUE,
    department_name TEXT NOT NULL UNIQUE,
    dean TEXT,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE programs (
    id UUID PRIMARY KEY,
    program_code VARCHAR(40) NOT NULL UNIQUE,
    program_name TEXT NOT NULL,
    department_id UUID NOT NULL REFERENCES departments(id),
    degree_type VARCHAR(40) NOT NULL,
    program_duration INTEGER,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE courses (
    id UUID PRIMARY KEY,
    course_code VARCHAR(40) NOT NULL UNIQUE,
    course_title TEXT NOT NULL,
    course_description TEXT,
    lecture_hours_per_week NUMERIC(8, 2) NOT NULL DEFAULT 0,
    laboratory_hours_per_week NUMERIC(8, 2) NOT NULL DEFAULT 0,
    credit_units NUMERIC(8, 2) NOT NULL DEFAULT 0,
    course_type VARCHAR(40) NOT NULL,
    department_id UUID NOT NULL REFERENCES departments(id),
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT courses_non_negative_hours CHECK (
        lecture_hours_per_week >= 0
        AND laboratory_hours_per_week >= 0
        AND credit_units >= 0
    )
);

CREATE TABLE school_years (
    id UUID PRIMARY KEY,
    school_year VARCHAR(20) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE semesters (
    id UUID PRIMARY KEY,
    name VARCHAR(40) NOT NULL UNIQUE,
    sort_order INTEGER NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE rooms (
    id UUID PRIMARY KEY,
    room_code VARCHAR(40) NOT NULL UNIQUE,
    room_name TEXT NOT NULL,
    capacity INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE faculty (
    id UUID PRIMARY KEY,
    employee_number VARCHAR(60) NOT NULL UNIQUE,
    first_name TEXT NOT NULL,
    middle_name TEXT,
    last_name TEXT NOT NULL,
    suffix TEXT,
    email TEXT NOT NULL UNIQUE,
    contact_number TEXT,
    department_id UUID NOT NULL REFERENCES departments(id),
    employment_status VARCHAR(40) NOT NULL,
    faculty_type VARCHAR(40) NOT NULL,
    specialization TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE sections (
    id UUID PRIMARY KEY,
    section_code VARCHAR(60) NOT NULL,
    program_id UUID NOT NULL REFERENCES programs(id),
    school_year_id UUID NOT NULL REFERENCES school_years(id),
    semester_id UUID NOT NULL REFERENCES semesters(id),
    year_level INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT sections_unique_term UNIQUE (section_code, school_year_id, semester_id)
);

CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID REFERENCES users(id),
    action VARCHAR(120) NOT NULL,
    module VARCHAR(120) NOT NULL,
    entity_type VARCHAR(120),
    entity_id UUID,
    old_value JSONB,
    new_value JSONB,
    ip_address VARCHAR(80),
    user_agent TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE generated_reports (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_type VARCHAR(120) NOT NULL,
    target_entity_type VARCHAR(120) NOT NULL,
    target_entity_id UUID NOT NULL,
    generated_by UUID REFERENCES users(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

INSERT INTO permissions (id, name, description) VALUES
('00000000-0000-0000-0000-000000000101', 'USER_MANAGE', 'Can manage system users'),
('00000000-0000-0000-0000-000000000102', 'ACADEMIC_SETUP_VIEW', 'Can view academic setup records'),
('00000000-0000-0000-0000-000000000103', 'ACADEMIC_SETUP_MANAGE', 'Can manage academic setup records'),
('00000000-0000-0000-0000-000000000104', 'STUDENT_CREATE', 'Can create student profiles'),
('00000000-0000-0000-0000-000000000105', 'STUDENT_UPDATE', 'Can update student profiles'),
('00000000-0000-0000-0000-000000000106', 'STUDENT_VIEW', 'Can view student records'),
('00000000-0000-0000-0000-000000000107', 'ENROLLMENT_CREATE', 'Can create enrollment records'),
('00000000-0000-0000-0000-000000000108', 'ENROLLMENT_APPROVE', 'Can approve enrollment'),
('00000000-0000-0000-0000-000000000109', 'GRADE_ENCODE', 'Can encode grades'),
('00000000-0000-0000-0000-000000000110', 'GRADE_APPROVE', 'Can approve and lock grades'),
('00000000-0000-0000-0000-000000000111', 'FEE_MANAGE', 'Can manage fee setup'),
('00000000-0000-0000-0000-000000000112', 'REPORT_GENERATE', 'Can generate reports'),
('00000000-0000-0000-0000-000000000113', 'AUDIT_VIEW', 'Can view audit logs');

INSERT INTO roles (id, name, description) VALUES
('00000000-0000-0000-0000-000000000201', 'SUPER_ADMIN', 'Full system administrator'),
('00000000-0000-0000-0000-000000000202', 'REGISTRAR', 'Registrar staff'),
('00000000-0000-0000-0000-000000000203', 'DEAN', 'Department dean'),
('00000000-0000-0000-0000-000000000204', 'PROGRAM_HEAD', 'Program head'),
('00000000-0000-0000-0000-000000000205', 'FACULTY', 'Faculty member'),
('00000000-0000-0000-0000-000000000206', 'CASHIER', 'Cashier or finance staff'),
('00000000-0000-0000-0000-000000000207', 'STUDENT', 'Student portal user'),
('00000000-0000-0000-0000-000000000208', 'READ_ONLY_STAFF', 'Read-only staff user');

INSERT INTO role_permissions (role_id, permission_id)
SELECT '00000000-0000-0000-0000-000000000201', id FROM permissions;

INSERT INTO role_permissions (role_id, permission_id) VALUES
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000102'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000103'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000104'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000105'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000106'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000107'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000108'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000110'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000112'),
('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000102'),
('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000109'),
('00000000-0000-0000-0000-000000000206', '00000000-0000-0000-0000-000000000102'),
('00000000-0000-0000-0000-000000000206', '00000000-0000-0000-0000-000000000111'),
('00000000-0000-0000-0000-000000000208', '00000000-0000-0000-0000-000000000102'),
('00000000-0000-0000-0000-000000000208', '00000000-0000-0000-0000-000000000106');

INSERT INTO users (id, email, username, password_hash, full_name, active) VALUES
(
    '00000000-0000-0000-0000-000000000301',
    'admin@sis.local',
    'admin',
    '$2a$10$yvMPa69o1Sqv/ge5WiRiAOAuT687Ufnk4aUqwZEOqm9VjffF5B8da',
    'System Administrator',
    true
);

INSERT INTO user_roles (user_id, role_id) VALUES
('00000000-0000-0000-0000-000000000301', '00000000-0000-0000-0000-000000000201');

INSERT INTO semesters (id, name, sort_order, active) VALUES
('00000000-0000-0000-0000-000000000401', 'FIRST_SEMESTER', 1, true),
('00000000-0000-0000-0000-000000000402', 'SECOND_SEMESTER', 2, true),
('00000000-0000-0000-0000-000000000403', 'SUMMER', 3, true);
