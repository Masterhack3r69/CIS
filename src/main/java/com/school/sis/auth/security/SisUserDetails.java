package com.school.sis.auth.security;

import com.school.sis.auth.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.UUID;
import java.util.stream.Stream;

public class SisUserDetails implements UserDetails {

    private final User user;

    public SisUserDetails(User user) {
        this.user = user;
    }

    public UUID id() {
        return user.getId();
    }

    public String email() {
        return user.getEmail();
    }

    public String fullName() {
        return user.getFullName();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getRoles().stream()
                .flatMap(role -> Stream.concat(
                        Stream.of(new SimpleGrantedAuthority("ROLE_" + role.getName())),
                        role.getPermissions().stream()
                                .map(permission -> new SimpleGrantedAuthority(permission.getName()))
                ))
                .distinct()
                .toList();
    }

    @Override
    public String getPassword() {
        return user.getPasswordHash();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
    }

    @Override
    public boolean isEnabled() {
        return user.isActive();
    }
}
