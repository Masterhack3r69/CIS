package com.school.sis.auth.controller;

import com.school.sis.auth.dto.AuthResponse;
import com.school.sis.auth.dto.LoginRequest;
import com.school.sis.auth.dto.RefreshRequest;
import com.school.sis.auth.dto.UserSummary;
import com.school.sis.auth.security.SisUserDetails;
import com.school.sis.auth.service.AuthService;
import com.school.sis.common.response.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.success("Login successful", authService.login(request));
    }

    @PostMapping("/refresh")
    public ApiResponse<AuthResponse> refresh(@Valid @RequestBody RefreshRequest request) {
        return ApiResponse.success("Token refreshed", authService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@Valid @RequestBody RefreshRequest request) {
        authService.logout(request);
        return ApiResponse.success("Logout successful");
    }

    @GetMapping("/me")
    public ApiResponse<UserSummary> me(@AuthenticationPrincipal SisUserDetails userDetails) {
        return ApiResponse.success("User retrieved", authService.summarize(userDetails));
    }
}
