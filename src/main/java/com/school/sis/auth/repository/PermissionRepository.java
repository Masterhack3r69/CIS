package com.school.sis.auth.repository;

import com.school.sis.auth.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {
}
