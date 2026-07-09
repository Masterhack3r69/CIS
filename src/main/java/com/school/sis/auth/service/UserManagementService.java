package com.school.sis.auth.service;

import com.school.sis.auth.dto.RoleResponse;
import com.school.sis.auth.dto.UserManagementRequest;
import com.school.sis.auth.dto.UserStatusRequest;
import com.school.sis.auth.dto.UserSummary;
import com.school.sis.auth.entity.Permission;
import com.school.sis.auth.entity.Role;
import com.school.sis.auth.entity.User;
import com.school.sis.auth.repository.RoleRepository;
import com.school.sis.auth.repository.UserRepository;
import com.school.sis.common.exception.BusinessRuleException;
import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.response.PageResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserManagementService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserSummary> listUsers(String search, Pageable pageable) {
        String term = search == null ? "" : search;
        return PageResponse.from(userRepository
                .findByUsernameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrFullNameContainingIgnoreCase(term, term, term, pageable)
                .map(this::toSummary));
    }

    @Transactional(readOnly = true)
    public UserSummary getUser(UUID id) {
        return toSummary(findUser(id));
    }

    @Transactional
    public UserSummary createUser(UserManagementRequest request) {
        validateUniqueUsername(request.username(), null);
        validateUniqueEmail(request.email(), null);
        if (request.password() == null || request.password().isBlank()) {
            throw new BusinessRuleException("Password is required for new users");
        }

        User user = new User();
        apply(user, request, true);
        return toSummary(userRepository.save(user));
    }

    @Transactional
    public UserSummary updateUser(UUID id, UserManagementRequest request) {
        validateUniqueUsername(request.username(), id);
        validateUniqueEmail(request.email(), id);
        User user = findUser(id);
        apply(user, request, false);
        return toSummary(user);
    }

    @Transactional
    public UserSummary updateStatus(UUID id, UserStatusRequest request) {
        User user = findUser(id);
        user.setActive(request.active());
        return toSummary(user);
    }

    @Transactional(readOnly = true)
    public PageResponse<RoleResponse> listRoles(Pageable pageable) {
        return PageResponse.from(roleRepository.findAll(pageable).map(this::toRoleResponse));
    }

    private void apply(User user, UserManagementRequest request, boolean creating) {
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setActive(request.active() == null || request.active());
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        } else if (creating) {
            throw new BusinessRuleException("Password is required for new users");
        }
        user.setRoles(resolveRoles(request.roleIds()));
    }

    private Set<Role> resolveRoles(Set<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            return Set.of();
        }
        return roleIds.stream()
                .map(id -> roleRepository.findById(id).orElseThrow(() -> new NotFoundException("Role not found")))
                .collect(Collectors.toSet());
    }

    private void validateUniqueUsername(String username, UUID currentUserId) {
        boolean exists = currentUserId == null
                ? userRepository.existsByUsernameIgnoreCase(username)
                : userRepository.existsByUsernameIgnoreCaseAndIdNot(username, currentUserId);
        if (exists) {
            throw new BusinessRuleException("Username already exists");
        }
    }

    private void validateUniqueEmail(String email, UUID currentUserId) {
        boolean exists = currentUserId == null
                ? userRepository.existsByEmailIgnoreCase(email)
                : userRepository.existsByEmailIgnoreCaseAndIdNot(email, currentUserId);
        if (exists) {
            throw new BusinessRuleException("Email already exists");
        }
    }

    private User findUser(UUID id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
    }

    private UserSummary toSummary(User user) {
        return new UserSummary(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.isActive(),
                user.getRoles().stream().map(Role::getName).sorted().toList(),
                user.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .map(Permission::getName)
                        .distinct()
                        .sorted(Comparator.naturalOrder())
                        .toList()
        );
    }

    private RoleResponse toRoleResponse(Role role) {
        return new RoleResponse(
                role.getId(),
                role.getName(),
                role.getDescription(),
                role.getPermissions().stream()
                        .map(Permission::getName)
                        .sorted()
                        .toList()
        );
    }
}
