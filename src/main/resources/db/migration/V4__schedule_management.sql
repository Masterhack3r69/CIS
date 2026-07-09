CREATE TABLE class_schedules (
    id UUID PRIMARY KEY,
    section_id UUID NOT NULL REFERENCES sections(id),
    course_id UUID NOT NULL REFERENCES courses(id),
    faculty_id UUID NOT NULL REFERENCES faculty(id),
    room_id UUID NOT NULL REFERENCES rooms(id),
    school_year_id UUID NOT NULL REFERENCES school_years(id),
    semester_id UUID NOT NULL REFERENCES semesters(id),
    capacity INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT class_schedules_capacity_positive CHECK (capacity IS NULL OR capacity > 0)
);

CREATE INDEX idx_class_schedules_section_id ON class_schedules(section_id);
CREATE INDEX idx_class_schedules_course_id ON class_schedules(course_id);
CREATE INDEX idx_class_schedules_faculty_id ON class_schedules(faculty_id);
CREATE INDEX idx_class_schedules_room_id ON class_schedules(room_id);
CREATE INDEX idx_class_schedules_term ON class_schedules(school_year_id, semester_id);
CREATE INDEX idx_class_schedules_status ON class_schedules(status);

CREATE TABLE schedule_meetings (
    id UUID PRIMARY KEY,
    class_schedule_id UUID NOT NULL REFERENCES class_schedules(id) ON DELETE CASCADE,
    day_of_week VARCHAR(20) NOT NULL,
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT schedule_meetings_valid_time_range CHECK (end_time > start_time)
);

CREATE INDEX idx_schedule_meetings_schedule_id ON schedule_meetings(class_schedule_id);
CREATE INDEX idx_schedule_meetings_day_time ON schedule_meetings(day_of_week, start_time, end_time);

INSERT INTO permissions (id, name, description) VALUES
('00000000-0000-0000-0000-000000000116', 'SCHEDULE_VIEW', 'Can view class schedules'),
('00000000-0000-0000-0000-000000000117', 'SCHEDULE_MANAGE', 'Can manage class schedules');

INSERT INTO role_permissions (role_id, permission_id) VALUES
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000116'),
('00000000-0000-0000-0000-000000000201', '00000000-0000-0000-0000-000000000117'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000116'),
('00000000-0000-0000-0000-000000000202', '00000000-0000-0000-0000-000000000117'),
('00000000-0000-0000-0000-000000000203', '00000000-0000-0000-0000-000000000116'),
('00000000-0000-0000-0000-000000000204', '00000000-0000-0000-0000-000000000116'),
('00000000-0000-0000-0000-000000000205', '00000000-0000-0000-0000-000000000116')
ON CONFLICT DO NOTHING;
