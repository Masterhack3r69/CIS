package com.school.sis.auth.dto;

import com.school.sis.auth.entity.Role;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record RoleResponse(UUID id, String name, String description, List<PermissionResponse> permissions) {
    public static RoleResponse from(Role role) {
        return new RoleResponse(role.getId(), role.getName(), role.getDescription(), role.getPermissions().stream()
                .sorted(Comparator.comparing(p -> p.getName())).map(PermissionResponse::from).toList());
    }
}
