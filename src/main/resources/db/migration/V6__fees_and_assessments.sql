CREATE TABLE fee_items (
    id UUID PRIMARY KEY,
    fee_code VARCHAR(60) NOT NULL UNIQUE,
    fee_name TEXT NOT NULL,
    category VARCHAR(40) NOT NULL,
    description TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX idx_fee_items_category ON fee_items(category);
CREATE INDEX idx_fee_items_status ON fee_items(status);

CREATE TABLE fee_rules (
    id UUID PRIMARY KEY,
    fee_item_id UUID NOT NULL REFERENCES fee_items(id),
    school_year_id UUID NOT NULL REFERENCES school_years(id),
    semester_id UUID REFERENCES semesters(id),
    program_id UUID REFERENCES programs(id),
    year_level INTEGER,
    computation_type VARCHAR(40) NOT NULL,
    amount NUMERIC(12, 2) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT fee_rules_amount_non_negative CHECK (amount >= 0),
    CONSTRAINT fee_rules_year_level_positive CHECK (year_level IS NULL OR year_level > 0)
);

CREATE INDEX idx_fee_rules_fee_item_id ON fee_rules(fee_item_id);
CREATE INDEX idx_fee_rules_school_year_id ON fee_rules(school_year_id);
CREATE INDEX idx_fee_rules_semester_id ON fee_rules(semester_id);
CREATE INDEX idx_fee_rules_program_id ON fee_rules(program_id);
CREATE INDEX idx_fee_rules_status ON fee_rules(status);

CREATE TABLE assessments (
    id UUID PRIMARY KEY,
    student_id UUID NOT NULL REFERENCES students(id),
    enrollment_id UUID NOT NULL REFERENCES enrollments(id),
    school_year_id UUID NOT NULL REFERENCES school_years(id),
    semester_id UUID NOT NULL REFERENCES semesters(id),
    total_units NUMERIC(8, 2) NOT NULL DEFAULT 0,
    tuition_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    laboratory_fee_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    miscellaneous_fee_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    other_fee_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    discount_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    penalty_amount NUMERIC(12, 2) NOT NULL DEFAULT 0,
    total_assessment NUMERIC(12, 2) NOT NULL DEFAULT 0,
    amount_paid NUMERIC(12, 2) NOT NULL DEFAULT 0,
    balance NUMERIC(12, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'UNPAID',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT assessments_amounts_non_negative CHECK (
        total_units >= 0
        AND tuition_amount >= 0
        AND laboratory_fee_amount >= 0
        AND miscellaneous_fee_amount >= 0
        AND other_fee_amount >= 0
        AND discount_amount >= 0
        AND penalty_amount >= 0
        AND total_assessment >= 0
        AND amount_paid >= 0
        AND balance >= 0
    )
);

CREATE UNIQUE INDEX ux_assessments_enrollment_id ON assessments(enrollment_id);
CREATE INDEX idx_assessments_student_id ON assessments(student_id);
CREATE INDEX idx_assessments_term ON assessments(school_year_id, semester_id);
CREATE INDEX idx_assessments_status ON assessments(status);

CREATE TABLE assessment_items (
    id UUID PRIMARY KEY,
    assessment_id UUID NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    fee_item_id UUID REFERENCES fee_items(id),
    enrollment_subject_id UUID REFERENCES enrollment_subjects(id),
    description TEXT NOT NULL,
    category VARCHAR(40) NOT NULL,
    computation_type VARCHAR(40) NOT NULL,
    quantity NUMERIC(10, 2) NOT NULL,
    unit_amount NUMERIC(12, 2) NOT NULL,
    total_amount NUMERIC(12, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT assessment_items_amounts_non_negative CHECK (
        quantity >= 0
        AND unit_amount >= 0
        AND total_amount >= 0
    )
);

CREATE INDEX idx_assessment_items_assessment_id ON assessment_items(assessment_id);
CREATE INDEX idx_assessment_items_fee_item_id ON assessment_items(fee_item_id);
CREATE INDEX idx_assessment_items_enrollment_subject_id ON assessment_items(enrollment_subject_id);
