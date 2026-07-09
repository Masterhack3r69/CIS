package com.school.sis.auth.repository;

import com.school.sis.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCaseOrUsernameIgnoreCase(String email, String username);
}
