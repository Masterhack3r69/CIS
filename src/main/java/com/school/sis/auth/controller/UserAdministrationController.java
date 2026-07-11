package com.school.sis.auth.controller;

import com.school.sis.auth.dto.*;
import com.school.sis.auth.service.UserAdministrationService;
import com.school.sis.common.response.ApiResponse;
import com.school.sis.common.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
@PreAuthorize("hasAuthority('USER_MANAGE')")
public class UserAdministrationController {
    private final UserAdministrationService service;
    public UserAdministrationController(UserAdministrationService service) { this.service = service; }

    @GetMapping("/users")
    public ApiResponse<PageResponse<UserResponse>> users(@RequestParam(required=false) String search,
            @RequestParam(required=false) UUID roleId, @RequestParam(required=false) UUID facultyId,
            @RequestParam(required=false) Boolean active, Pageable pageable) {
        return ApiResponse.success("Users retrieved", service.list(new UserSearchCriteria(search, roleId, facultyId, active), pageable));
    }
    @PostMapping("/users") public ApiResponse<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ApiResponse.success("User created", service.create(request));
    }
    @GetMapping("/users/{id}") public ApiResponse<UserResponse> get(@PathVariable UUID id) {
        return ApiResponse.success("User retrieved", service.get(id));
    }
    @PutMapping("/users/{id}") public ApiResponse<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UserRequest request) {
        return ApiResponse.success("User updated", service.update(id, request));
    }
    @PatchMapping("/users/{id}/status") public ApiResponse<UserResponse> status(@PathVariable UUID id, @Valid @RequestBody UserStatusRequest request) {
        return ApiResponse.success("User status updated", service.setStatus(id, request.active()));
    }
    @PostMapping("/users/{id}/reset-password") public ApiResponse<Void> reset(@PathVariable UUID id, @Valid @RequestBody PasswordResetRequest request) {
        service.resetPassword(id, request.newPassword()); return ApiResponse.success("Password reset", null);
    }
    @GetMapping("/roles") public ApiResponse<List<RoleResponse>> roles() {
        return ApiResponse.success("Roles retrieved", service.listRoles());
    }
    @PutMapping("/roles/{id}/permissions") public ApiResponse<RoleResponse> permissions(@PathVariable UUID id, @Valid @RequestBody RolePermissionsRequest request) {
        return ApiResponse.success("Role permissions updated", service.setRolePermissions(id, request.permissionIds()));
    }
}
