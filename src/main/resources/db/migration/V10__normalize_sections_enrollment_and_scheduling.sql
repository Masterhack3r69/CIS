ALTER TABLE student_contacts
    ADD COLUMN current_region_code VARCHAR(20),
    ADD COLUMN current_region_name TEXT,
    ADD COLUMN current_province_code VARCHAR(20),
    ADD COLUMN current_province_name TEXT,
    ADD COLUMN current_city_municipality_code VARCHAR(20),
    ADD COLUMN current_city_municipality_name TEXT,
    ADD COLUMN current_barangay_code VARCHAR(20),
    ADD COLUMN current_barangay_name TEXT,
    ADD COLUMN current_zip_code VARCHAR(20),
    ADD COLUMN permanent_region_code VARCHAR(20),
    ADD COLUMN permanent_region_name TEXT,
    ADD COLUMN permanent_province_code VARCHAR(20),
    ADD COLUMN permanent_province_name TEXT,
    ADD COLUMN permanent_city_municipality_code VARCHAR(20),
    ADD COLUMN permanent_city_municipality_name TEXT,
    ADD COLUMN permanent_barangay_code VARCHAR(20),
    ADD COLUMN permanent_barangay_name TEXT,
    ADD COLUMN permanent_zip_code VARCHAR(20);

UPDATE student_contacts SET
    current_region_code = region_code,
    current_region_name = region_name,
    current_province_code = province_code,
    current_province_name = province_name,
    current_city_municipality_code = city_municipality_code,
    current_city_municipality_name = city_municipality_name,
    current_barangay_code = barangay_code,
    current_barangay_name = barangay_name,
    current_zip_code = zip_code;

ALTER TABLE sections ADD COLUMN curriculum_id UUID REFERENCES curricula(id);

UPDATE sections s
SET curriculum_id = candidate.curriculum_id
FROM (
    SELECT program_id, (array_agg(id ORDER BY id::text))[1] AS curriculum_id
    FROM curricula
    GROUP BY program_id
    HAVING COUNT(*) = 1
) candidate
WHERE candidate.program_id = s.program_id;

UPDATE sections SET status = 'INACTIVE' WHERE curriculum_id IS NULL;

ALTER TABLE enrollments ADD COLUMN year_level INTEGER;
UPDATE enrollments e SET year_level = s.year_level FROM students s WHERE s.id = e.student_id;
ALTER TABLE enrollments ALTER COLUMN year_level SET NOT NULL;
ALTER TABLE enrollments ADD CONSTRAINT enrollments_year_level_positive CHECK (year_level > 0);

UPDATE class_schedules SET capacity = 1 WHERE capacity IS NULL OR capacity < 1;
ALTER TABLE class_schedules ALTER COLUMN capacity SET NOT NULL;

ALTER TABLE students DROP CONSTRAINT IF EXISTS students_section_id_fkey;
DROP INDEX IF EXISTS idx_students_section_id;
ALTER TABLE students DROP COLUMN IF EXISTS section_id;
ALTER TABLE students DROP COLUMN IF EXISTS semester;

CREATE INDEX idx_sections_curriculum_id ON sections(curriculum_id);
CREATE INDEX idx_enrollments_year_level ON enrollments(year_level);
