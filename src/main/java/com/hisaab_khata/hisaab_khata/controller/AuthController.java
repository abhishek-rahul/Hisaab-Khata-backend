package com.hisaab_khata.hisaab_khata.controller;

import com.hisaab_khata.hisaab_khata.dto.SuccessResponse;
import com.hisaab_khata.hisaab_khata.dto.authdto.*;
import com.hisaab_khata.hisaab_khata.service.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    @Autowired
    private final IAuthService authService;

    @PostMapping("/register")
    public ResponseEntity<SuccessResponse<AuthResponse>> register(
            @RequestBody @Valid RegisterRequest request
    ) {
        AuthResponse response = authService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SuccessResponse.<AuthResponse>builder()
                        .success(true)
                        .status(HttpStatus.CREATED.value())
                        .message("Shop registered successfully")
                        .data(response)
                        .build());
    }

    @PostMapping("/login")
    public ResponseEntity<SuccessResponse<AuthResponse>> login(
            @RequestBody @Valid LoginRequest request
    ) {
        AuthResponse response = authService.login(request);

        return ResponseEntity.ok(
                SuccessResponse.<AuthResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Login successful")
                        .data(response)
                        .build());
    }

    @PostMapping("/refresh")
    public ResponseEntity<SuccessResponse<AuthResponse>> refreshToken(
            @RequestHeader("Authorization") String bearerToken
    ) {
        String oldToken = bearerToken.replace("Bearer ", "");
        AuthResponse response = authService.refreshToken(oldToken);

        return ResponseEntity.ok(
                SuccessResponse.<AuthResponse>builder()
                        .success(true)
                        .status(HttpStatus.OK.value())
                        .message("Token refreshed")
                        .data(response)
                        .build());
    }
}
