package com.hisaab_khata.hisaab_khata.controller;

import com.hisaab_khata.hisaab_khata.dto.ApiResponse;
import com.hisaab_khata.hisaab_khata.dto.authdto.*;
import com.hisaab_khata.hisaab_khata.exception.UnauthorizedException;
import com.hisaab_khata.hisaab_khata.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@RequestBody @Valid RegisterRequest request) {
        AuthResponse response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created("Shop registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@RequestBody @Valid LoginRequest request) {
        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResponse.ok("Login successful", response));
    }

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(
            @RequestHeader(value = "Authorization", required = false) String bearerToken) {
        if (bearerToken == null || !bearerToken.startsWith("Bearer ")) {
            throw new UnauthorizedException(
                    "Authorization header with Bearer token required", "MISSING_REFRESH_TOKEN");
        }
        String oldToken = bearerToken.replace("Bearer ", "").trim();
        AuthResponse response = authService.refreshToken(oldToken);
        return ResponseEntity.ok(ApiResponse.ok("Token refreshed", response));
    }
}
