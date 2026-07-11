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

UPDATE student_contacts
SET current_province_name = province,
    current_city_municipality_name = city_municipality,
    current_barangay_name = barangay,
    current_zip_code = zip_code;
