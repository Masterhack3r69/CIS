package com.school.sis.auth.repository;

import com.school.sis.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {
    Optional<User> findByEmailIgnoreCaseOrUsernameIgnoreCase(String email, String username);
    boolean existsByEmailIgnoreCaseAndIdNot(String email, UUID id);
    boolean existsByUsernameIgnoreCaseAndIdNot(String username, UUID id);
    boolean existsByFacultyIdAndIdNot(UUID facultyId, UUID id);
    long countByActiveTrueAndRolesName(String roleName);
}
