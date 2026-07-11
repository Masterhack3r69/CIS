package com.school.sis.auth.dto;

import com.school.sis.auth.entity.User;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public record UserResponse(UUID id, String username, String email, String fullName, boolean active,
                           UUID facultyId, String facultyName, List<RoleResponse> roles,
                           Instant createdAt, Instant updatedAt) {
    public static UserResponse from(User user) {
        var faculty = user.getFaculty();
        String facultyName = faculty == null ? null : (faculty.getFirstName() + " " + faculty.getLastName()).trim();
        return new UserResponse(user.getId(), user.getUsername(), user.getEmail(), user.getFullName(), user.isActive(),
                faculty == null ? null : faculty.getId(), facultyName,
                user.getRoles().stream().sorted(Comparator.comparing(r -> r.getName())).map(RoleResponse::from).toList(),
                user.getCreatedAt(), user.getUpdatedAt());
    }
}
