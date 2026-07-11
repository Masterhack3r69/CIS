package com.school.sis.auth.service;

import com.school.sis.audit.AuditModule;
import com.school.sis.audit.service.AuditService;
import com.school.sis.auth.dto.*;
import com.school.sis.auth.entity.Permission;
import com.school.sis.auth.entity.Role;
import com.school.sis.auth.entity.User;
import com.school.sis.auth.repository.PermissionRepository;
import com.school.sis.auth.repository.RefreshTokenRepository;
import com.school.sis.auth.repository.RoleRepository;
import com.school.sis.auth.repository.UserRepository;
import com.school.sis.auth.security.SisUserDetails;
import com.school.sis.common.exception.BusinessRuleException;
import com.school.sis.common.exception.NotFoundException;
import com.school.sis.common.response.PageResponse;
import com.school.sis.setup.entity.Faculty;
import com.school.sis.setup.repository.FacultyRepository;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Service
public class UserAdministrationService {
    private static final String SUPER_ADMIN = "SUPER_ADMIN";
    private final UserRepository users;
    private final RoleRepository roles;
    private final PermissionRepository permissions;
    private final FacultyRepository faculty;
    private final RefreshTokenRepository refreshTokens;
    private final PasswordEncoder passwordEncoder;
    private final AuditService audit;

    public UserAdministrationService(UserRepository users, RoleRepository roles, PermissionRepository permissions,
            FacultyRepository faculty, RefreshTokenRepository refreshTokens, PasswordEncoder passwordEncoder,
            AuditService audit) {
        this.users = users; this.roles = roles; this.permissions = permissions; this.faculty = faculty;
        this.refreshTokens = refreshTokens; this.passwordEncoder = passwordEncoder; this.audit = audit;
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(UserSearchCriteria criteria, Pageable pageable) {
        return PageResponse.from(users.findAll(specification(criteria), pageable).map(UserResponse::from));
    }

    @Transactional(readOnly = true)
    public UserResponse get(UUID id) { return UserResponse.from(findUser(id)); }

    @Transactional
    public UserResponse create(UserRequest request) {
        if (request.initialPassword() == null || request.initialPassword().isBlank())
            throw new BusinessRuleException("Initial password is required");
        User user = new User();
        apply(user, request);
        user.setPasswordHash(passwordEncoder.encode(request.initialPassword()));
        user.setActive(true);
        User saved = users.save(user);
        UserResponse response = UserResponse.from(saved);
        audit.log("USER_CREATED", AuditModule.USER, "User", saved.getId(), null, auditSnapshot(response));
        return response;
    }

    @Transactional
    public UserResponse update(UUID id, UserRequest request) {
        User user = findUser(id);
        UserResponse before = UserResponse.from(user);
        Set<String> requestedRoleNames = resolveRoles(request.roleIds()).stream().map(Role::getName).collect(java.util.stream.Collectors.toSet());
        if (id.equals(currentUserId()) && hasRole(user, SUPER_ADMIN) && !requestedRoleNames.contains(SUPER_ADMIN))
            throw new BusinessRuleException("You cannot remove your own SUPER_ADMIN role");
        apply(user, request);
        UserResponse after = UserResponse.from(user);
        audit.log("USER_UPDATED", AuditModule.USER, "User", id, auditSnapshot(before), auditSnapshot(after));
        return after;
    }

    @Transactional
    public UserResponse setStatus(UUID id, boolean active) {
        User user = findUser(id);
        if (!active && id.equals(currentUserId())) throw new BusinessRuleException("You cannot deactivate your own account");
        if (!active && user.isActive() && hasRole(user, SUPER_ADMIN) && users.countByActiveTrueAndRolesName(SUPER_ADMIN) <= 1)
            throw new BusinessRuleException("At least one active SUPER_ADMIN must remain");
        boolean before = user.isActive();
        user.setActive(active);
        if (!active) refreshTokens.deleteByUserId(id);
        audit.log("USER_STATUS_UPDATED", AuditModule.USER, "User", id, Map.of("active", before), Map.of("active", active));
        return UserResponse.from(user);
    }

    @Transactional
    public void resetPassword(UUID id, String newPassword) {
        User user = findUser(id);
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        refreshTokens.deleteByUserId(id);
        audit.log("USER_PASSWORD_RESET", AuditModule.USER, "User", id, null, Map.of("refreshTokensRevoked", true));
    }

    @Transactional(readOnly = true)
    public List<RoleResponse> listRoles() {
        return roles.findAll().stream().sorted(Comparator.comparing(Role::getName)).map(RoleResponse::from).toList();
    }

    @Transactional
    public RoleResponse setRolePermissions(UUID id, Set<UUID> permissionIds) {
        Role role = roles.findById(id).orElseThrow(() -> new NotFoundException("Role not found"));
        Set<String> before = role.getPermissions().stream().map(Permission::getName).collect(java.util.stream.Collectors.toCollection(TreeSet::new));
        List<Permission> found = permissions.findAllById(permissionIds);
        if (found.size() != permissionIds.size()) throw new NotFoundException("One or more permissions were not found");
        role.setPermissions(new HashSet<>(found));
        Set<String> after = found.stream().map(Permission::getName).collect(java.util.stream.Collectors.toCollection(TreeSet::new));
        audit.log("ROLE_PERMISSIONS_UPDATED", AuditModule.USER, "Role", id, Map.of("permissions", before), Map.of("permissions", after));
        return RoleResponse.from(role);
    }

    private void apply(User user, UserRequest request) {
        String username = request.username().trim().toLowerCase(Locale.ROOT);
        String email = request.email().trim().toLowerCase(Locale.ROOT);
        UUID id = user.getId() == null ? new UUID(0, 0) : user.getId();
        if (users.existsByUsernameIgnoreCaseAndIdNot(username, id)) throw new BusinessRuleException("Username already exists");
        if (users.existsByEmailIgnoreCaseAndIdNot(email, id)) throw new BusinessRuleException("Email already exists");
        if (request.facultyId() != null && users.existsByFacultyIdAndIdNot(request.facultyId(), id))
            throw new BusinessRuleException("Faculty is already linked to another user");
        Set<Role> resolvedRoles = resolveRoles(request.roleIds());
        user.setUsername(username); user.setEmail(email); user.setFullName(request.fullName().trim());
        user.getRoles().clear(); user.getRoles().addAll(resolvedRoles);
        Faculty linkedFaculty = request.facultyId() == null ? null : faculty.findById(request.facultyId())
                .orElseThrow(() -> new NotFoundException("Faculty not found"));
        user.setFaculty(linkedFaculty);
    }

    private Set<Role> resolveRoles(Set<UUID> ids) {
        List<Role> found = roles.findAllById(ids);
        if (found.size() != ids.size()) throw new NotFoundException("One or more roles were not found");
        return new HashSet<>(found);
    }

    private User findUser(UUID id) { return users.findById(id).orElseThrow(() -> new NotFoundException("User not found")); }
    private boolean hasRole(User user, String name) { return user.getRoles().stream().anyMatch(r -> r.getName().equals(name)); }
    private UUID currentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof SisUserDetails details ? details.id() : null;
    }
    private Map<String, Object> auditSnapshot(UserResponse user) {
        return Map.of("username", user.username(), "email", user.email(), "fullName", user.fullName(),
                "active", user.active(), "roles", user.roles().stream().map(RoleResponse::name).toList(),
                "facultyId", user.facultyId() == null ? "" : user.facultyId().toString());
    }
    private Specification<User> specification(UserSearchCriteria criteria) {
        return (root, query, cb) -> {
            query.distinct(true); var predicate = cb.conjunction();
            if (criteria == null) return predicate;
            if (criteria.search() != null && !criteria.search().isBlank()) {
                String term = "%" + criteria.search().trim().toLowerCase(Locale.ROOT) + "%";
                predicate = cb.and(predicate, cb.or(cb.like(cb.lower(root.get("username")), term),
                        cb.like(cb.lower(root.get("email")), term), cb.like(cb.lower(root.get("fullName")), term)));
            }
            if (criteria.roleId() != null) predicate = cb.and(predicate, cb.equal(root.join("roles", JoinType.INNER).get("id"), criteria.roleId()));
            if (criteria.facultyId() != null) predicate = cb.and(predicate, cb.equal(root.get("faculty").get("id"), criteria.facultyId()));
            if (criteria.active() != null) predicate = cb.and(predicate, cb.equal(root.get("active"), criteria.active()));
            return predicate;
        };
    }
}
