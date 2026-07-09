package com.school.sis.auth.service;

import com.school.sis.auth.dto.AuthResponse;
import com.school.sis.auth.dto.LoginRequest;
import com.school.sis.auth.dto.RefreshRequest;
import com.school.sis.auth.dto.UserSummary;
import com.school.sis.auth.entity.RefreshToken;
import com.school.sis.auth.entity.User;
import com.school.sis.auth.repository.RefreshTokenRepository;
import com.school.sis.auth.repository.UserRepository;
import com.school.sis.auth.security.JwtProperties;
import com.school.sis.auth.security.JwtService;
import com.school.sis.auth.security.SisUserDetails;
import com.school.sis.common.exception.BusinessRuleException;
import com.school.sis.common.exception.NotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Comparator;
import java.util.UUID;

@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final JwtProperties jwtProperties;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public AuthService(
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            JwtProperties jwtProperties,
            UserRepository userRepository,
            RefreshTokenRepository refreshTokenRepository
    ) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.jwtProperties = jwtProperties;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.usernameOrEmail(), request.password())
        );
        SisUserDetails userDetails = (SisUserDetails) authentication.getPrincipal();
        User user = userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(userDetails.getUsername(), userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return issueTokens(user);
    }

    @Transactional
    public AuthResponse refresh(RefreshRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new BusinessRuleException("Invalid refresh token"));
        if (!refreshToken.isUsable()) {
            throw new BusinessRuleException("Refresh token is expired or revoked");
        }
        refreshToken.revoke();
        return issueTokens(refreshToken.getUser());
    }

    @Transactional
    public void logout(RefreshRequest request) {
        refreshTokenRepository.findByToken(request.refreshToken()).ifPresent(RefreshToken::revoke);
    }

    public UserSummary summarize(SisUserDetails userDetails) {
        User user = userRepository.findByEmailIgnoreCaseOrUsernameIgnoreCase(userDetails.getUsername(), userDetails.getUsername())
                .orElseThrow(() -> new NotFoundException("User not found"));
        return toSummary(user);
    }

    private AuthResponse issueTokens(User user) {
        SisUserDetails userDetails = new SisUserDetails(user);
        String accessToken = jwtService.createAccessToken(userDetails);
        RefreshToken refreshToken = new RefreshToken(
                UUID.randomUUID(),
                randomToken(),
                user,
                Instant.now().plusSeconds(jwtProperties.refreshExpirationSeconds())
        );
        refreshTokenRepository.save(refreshToken);
        return new AuthResponse(
                accessToken,
                refreshToken.getToken(),
                jwtProperties.accessExpirationSeconds(),
                toSummary(user)
        );
    }

    private String randomToken() {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private UserSummary toSummary(User user) {
        return new UserSummary(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.isActive(),
                user.getRoles().stream().map(role -> role.getName()).sorted().toList(),
                user.getRoles().stream()
                        .flatMap(role -> role.getPermissions().stream())
                        .map(permission -> permission.getName())
                        .distinct()
                        .sorted(Comparator.naturalOrder())
                        .toList()
        );
    }
}
