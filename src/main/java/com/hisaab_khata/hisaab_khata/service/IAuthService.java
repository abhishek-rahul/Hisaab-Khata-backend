package com.hisaab_khata.hisaab_khata.service;


import com.hisaab_khata.hisaab_khata.dto.authdto.*;

public interface IAuthService {

/*    RegisterResponse register(RegisterRequest request);

    LoginResponse login(LoginRequest request);

    RefreshTokenResponse refreshToken(RefreshTokenRequest request);*/


    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(String oldToken);
}

