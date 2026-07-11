package com.school.sis.auth.dto;

import jakarta.validation.constraints.NotNull;

import java.util.Set;
import java.util.UUID;

public record RolePermissionsRequest(@NotNull Set<UUID> permissionIds) {}
