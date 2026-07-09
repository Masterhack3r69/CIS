CREATE TABLE fee_items (
    id UUID PRIMARY KEY,
    fee_code VARCHAR(60) NOT NULL UNIQUE,
    fee_name TEXT NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE fee_rules (
    id UUID PRIMARY KEY,
    fee_item_id UUID NOT NULL REFERENCES fee_items(id),
    rule_type VARCHAR(30) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    program_id UUID REFERENCES programs(id),
    school_year_id UUID REFERENCES school_years(id),
    semester_id UUID REFERENCES semesters(id),
    year_level INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fee_rules_amount_non_negative CHECK (amount >= 0),
    CONSTRAINT fee_rules_year_level_positive CHECK (year_level IS NULL OR year_level > 0)
);

CREATE INDEX idx_fee_rules_fee_item_id ON fee_rules(fee_item_id);
CREATE INDEX idx_fee_rules_scope ON fee_rules(program_id, school_year_id, semester_id, year_level);
CREATE INDEX idx_fee_rules_status ON fee_rules(status);

CREATE TABLE assessments (
    id UUID PRIMARY KEY,
    assessment_number VARCHAR(60) NOT NULL UNIQUE,
    enrollment_id UUID NOT NULL REFERENCES enrollments(id),
    student_id UUID NOT NULL REFERENCES students(id),
    school_year_id UUID NOT NULL REFERENCES school_years(id),
    semester_id UUID NOT NULL REFERENCES semesters(id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    total_units NUMERIC(8, 2) NOT NULL DEFAULT 0,
    subtotal_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    remarks TEXT,
    generated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT assessments_non_negative_amounts CHECK (
        total_units >= 0
        AND subtotal_amount >= 0
        AND discount_amount >= 0
        AND total_amount >= 0
    )
);

CREATE INDEX idx_assessments_enrollment_id ON assessments(enrollment_id);
CREATE INDEX idx_assessments_student_id ON assessments(student_id);
CREATE INDEX idx_assessments_term ON assessments(school_year_id, semester_id);
CREATE INDEX idx_assessments_status ON assessments(status);
CREATE UNIQUE INDEX ux_assessments_active_enrollment
    ON assessments(enrollment_id)
    WHERE status <> 'VOID';

CREATE TABLE assessment_items (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    fee_item_id UUID REFERENCES fee_items(id),
    fee_code VARCHAR(60) NOT NULL,
    fee_name TEXT NOT NULL,
    rule_type VARCHAR(30) NOT NULL,
    quantity NUMERIC(10, 2) NOT NULL,
    unit_amount NUMERIC(12, 2) NOT NULL,
    line_amount NUMERIC(12, 2) NOT NULL,
    sort_order INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT assessment_items_non_negative_amounts CHECK (
        quantity >= 0
        AND unit_amount >= 0
        AND line_amount >= 0
    )
);

CREATE INDEX idx_assessment_items_assessment_id ON assessment_items(assessment_id);

INSERT INTO permissions (id, name, description) VALUES
('00000000-0000-0000-0000-000000000119', 'FEE_VIEW', 'Can view fees and assessments')
ON CONFLICT DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id) VALUES
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000119'),
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000111'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000119'),
('00000000-0000-0000-0000-000000000206', '00000000-0000-0000-0000-000000000119'),
('00000000-0000-0000-0000-000000000206', '00000000-0000-0000-0000-000000000111')
ON CONFLICT DO NOTHING;
