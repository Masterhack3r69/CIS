package com.school.sis.student.repository;

import com.school.sis.student.entity.StudentContact;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface StudentContactRepository extends JpaRepository<StudentContact, UUID> {
    boolean existsByEmailAddressIgnoreCase(String emailAddress);
    boolean existsByEmailAddressIgnoreCaseAndStudentIdNot(String emailAddress, UUID studentId);
}
