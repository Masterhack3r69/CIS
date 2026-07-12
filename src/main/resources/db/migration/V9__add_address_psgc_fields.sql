ALTER TABLE student_contacts RENAME COLUMN province TO province_name;
ALTER TABLE student_contacts RENAME COLUMN city_municipality TO city_municipality_name;
ALTER TABLE student_contacts RENAME COLUMN barangay TO barangay_name;

ALTER TABLE student_contacts
    ADD COLUMN region_code VARCHAR(20),
    ADD COLUMN region_name TEXT,
    ADD COLUMN province_code VARCHAR(20),
    ADD COLUMN city_municipality_code VARCHAR(20),
    ADD COLUMN barangay_code VARCHAR(20);
