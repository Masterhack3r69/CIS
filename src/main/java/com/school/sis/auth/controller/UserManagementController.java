package com.school.sis.auth.controller;

import com.school.sis.auth.dto.RoleResponse;
import com.school.sis.auth.dto.UserManagementRequest;
import com.school.sis.auth.dto.UserStatusRequest;
import com.school.sis.auth.dto.UserSummary;
import com.school.sis.auth.service.UserManagementService;
import com.school.sis.common.response.ApiResponse;
import com.school.sis.common.response.PageResponse;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class UserManagementController {

    private final UserManagementService userManagementService;

    public UserManagementController(UserManagementService userManagementService) {
        this.userManagementService = userManagementService;
    }

    @GetMapping("/api/v1/users")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<PageResponse<UserSummary>> listUsers(@RequestParam(required = false) String search, Pageable pageable) {
        return ApiResponse.success("Users retrieved", userManagementService.listUsers(search, pageable));
    }

    @PostMapping("/api/v1/users")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<UserSummary> createUser(@Valid @RequestBody UserManagementRequest request) {
        return ApiResponse.success("User created", userManagementService.createUser(request));
    }

    @GetMapping("/api/v1/users/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<UserSummary> getUser(@PathVariable UUID id) {
        return ApiResponse.success("User retrieved", userManagementService.getUser(id));
    }

    @PutMapping("/api/v1/users/{id}")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<UserSummary> updateUser(@PathVariable UUID id, @Valid @RequestBody UserManagementRequest request) {
        return ApiResponse.success("User updated", userManagementService.updateUser(id, request));
    }

    @PatchMapping("/api/v1/users/{id}/status")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<UserSummary> updateStatus(@PathVariable UUID id, @Valid @RequestBody UserStatusRequest request) {
        return ApiResponse.success("User status updated", userManagementService.updateStatus(id, request));
    }

    @GetMapping("/api/v1/roles")
    @PreAuthorize("hasAuthority('USER_MANAGE')")
    public ApiResponse<PageResponse<RoleResponse>> listRoles(Pageable pageable) {
        return ApiResponse.success("Roles retrieved", userManagementService.listRoles(pageable));
    }
}
