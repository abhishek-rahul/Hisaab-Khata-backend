package com.hisaab_khata.hisaab_khata.service;

import com.hisaab_khata.hisaab_khata.dto.authdto.*;

public interface IAuthService {

    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String oldToken);
    StaffResponse createStaff(Long shopId, StaffCreateRequest request);
}

