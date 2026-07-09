CREATE TABLE curricula (
    id UUID PRIMARY KEY,
    program_id UUID NOT NULL REFERENCES programs(id),
    curriculum_code VARCHAR(60) NOT NULL UNIQUE,
    curriculum_name TEXT NOT NULL,
    effective_school_year VARCHAR(20) NOT NULL,
    version VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    description TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_curricula_program_id ON curricula(program_id);
CREATE UNIQUE INDEX ux_curricula_one_active_per_program
    ON curricula(program_id)
    WHERE status = 'ACTIVE';

CREATE TABLE curriculum_courses (
    id UUID PRIMARY KEY,
    curriculum_id UUID NOT NULL REFERENCES curricula(id) ON DELETE CASCADE,
    year_level INTEGER NOT NULL,
    semester VARCHAR(40) NOT NULL,
    course_id UUID NOT NULL REFERENCES courses(id),
    sort_order INTEGER NOT NULL,
    required_status VARCHAR(20) NOT NULL DEFAULT 'REQUIRED',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT curriculum_courses_year_level_positive CHECK (year_level > 0),
    CONSTRAINT curriculum_courses_sort_order_positive CHECK (sort_order > 0),
    CONSTRAINT curriculum_courses_unique_term_course UNIQUE (curriculum_id, year_level, semester, course_id)
);

CREATE INDEX idx_curriculum_courses_curriculum_id ON curriculum_courses(curriculum_id);
CREATE INDEX idx_curriculum_courses_course_id ON curriculum_courses(course_id);

CREATE TABLE curriculum_course_prerequisites (
    curriculum_course_id UUID NOT NULL REFERENCES curriculum_courses(id) ON DELETE CASCADE,
    prerequisite_course_id UUID NOT NULL REFERENCES courses(id),
    PRIMARY KEY (curriculum_course_id, prerequisite_course_id)
);

CREATE TABLE curriculum_course_corequisites (
    curriculum_course_id UUID NOT NULL REFERENCES curriculum_courses(id) ON DELETE CASCADE,
    corequisite_course_id UUID NOT NULL REFERENCES courses(id),
    PRIMARY KEY (curriculum_course_id, corequisite_course_id)
);

INSERT INTO permissions (id, name, description) VALUES
('00000000-0000-0000-0000-000000000114', 'CURRICULUM_VIEW', 'Can view curriculum records'),
('00000000-0000-0000-0000-000000000115', 'CURRICULUM_MANAGE', 'Can manage curriculum records');

INSERT INTO role_permissions (role_id, permission_id) VALUES
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000114'),
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000115'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000114'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000115'),
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000114'),
('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000114');
