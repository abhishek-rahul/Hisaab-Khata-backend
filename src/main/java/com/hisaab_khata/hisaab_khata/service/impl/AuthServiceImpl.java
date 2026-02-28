package com.hisaab_khata.hisaab_khata.service.impl;

import com.hisaab_khata.hisaab_khata.auth.JwtUtil;
import com.hisaab_khata.hisaab_khata.auth.ShopUserPrincipal;
import com.hisaab_khata.hisaab_khata.domain.AppUser;
import com.hisaab_khata.hisaab_khata.domain.AuthCredential;
import com.hisaab_khata.hisaab_khata.domain.Shop;
import com.hisaab_khata.hisaab_khata.domain.ShopUser;
import com.hisaab_khata.hisaab_khata.dto.authdto.AuthResponse;
import com.hisaab_khata.hisaab_khata.dto.authdto.LoginRequest;
import com.hisaab_khata.hisaab_khata.dto.authdto.RegisterRequest;
import com.hisaab_khata.hisaab_khata.enums.UserRole;
import com.hisaab_khata.hisaab_khata.enums.UserStatus;
import com.hisaab_khata.hisaab_khata.exception.BusinessValidationException;
import com.hisaab_khata.hisaab_khata.repository.AppUserRepository;
import com.hisaab_khata.hisaab_khata.repository.AuthCredentialRepository;
import com.hisaab_khata.hisaab_khata.repository.ShopRepository;
import com.hisaab_khata.hisaab_khata.repository.ShopUserRepository;
import com.hisaab_khata.hisaab_khata.service.IAuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    private final ShopRepository shopRepository;
    private final AppUserRepository appUserRepository;
    private final AuthCredentialRepository authCredentialRepository;
    private final ShopUserRepository shopUserRepository;
    private final JwtUtil jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest req) {
        if (appUserRepository.existsByPhone(req.getPhone())) {
            throw new BusinessValidationException("Phone already registered", "PHONE_EXISTS");
        }

        AppUser appUser = AppUser.builder()
                .name(req.getName())
                .phone(req.getPhone())
                .build();
        appUser = appUserRepository.save(appUser);

        AuthCredential credential = AuthCredential.builder()
                .appUser(appUser)
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .build();
        authCredentialRepository.save(credential);

        Shop shop = Shop.builder()
                .name(req.getShopName())
                .city(req.getCity())
                .build();
        shop = shopRepository.save(shop);

        ShopUser shopUser = ShopUser.builder()
                .shopId(shop.getId())
                .userId(appUser.getId())
                .role(UserRole.OWNER)
                .status(UserStatus.ACTIVE)
                .build();
        shopUserRepository.save(shopUser);

        ShopUserPrincipal principal = new ShopUserPrincipal(
                appUser.getId(), shop.getId(), appUser.getPhone(),
                credential.getPasswordHash(), UserRole.OWNER
        );
        String token = jwtTokenProvider.createToken(principal);

        return AuthResponse.builder()
                .shopId(shop.getId())
                .userId(appUser.getId())
                .token(token)
                .refreshToken(null)
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest req) {
        AppUser appUser = appUserRepository.findByPhone(req.getPhone())
                .orElseThrow(() -> new BusinessValidationException("Invalid phone or password", "INVALID_CREDENTIALS"));

        AuthCredential credential = authCredentialRepository.findById(appUser.getId())
                .orElseThrow(() -> new BusinessValidationException("Invalid phone or password", "INVALID_CREDENTIALS"));

        if (!passwordEncoder.matches(req.getPassword(), credential.getPasswordHash())) {
            throw new BusinessValidationException("Invalid phone or password", "INVALID_CREDENTIALS");
        }

        List<ShopUser> memberships = shopUserRepository.findByUserIdAndStatus(appUser.getId(), UserStatus.ACTIVE);
        if (memberships.isEmpty()) {
            throw new BusinessValidationException("No active shop for user", "NO_ACTIVE_SHOP");
        }
        ShopUser shopUser = memberships.get(0);

        ShopUserPrincipal principal = new ShopUserPrincipal(
                appUser.getId(), shopUser.getShopId(), appUser.getPhone(),
                credential.getPasswordHash(), shopUser.getRole()
        );
        String token = jwtTokenProvider.createToken(principal);

        return AuthResponse.builder()
                .shopId(shopUser.getShopId())
                .userId(appUser.getId())
                .token(token)
                .refreshToken(null)
                .build();
    }

    @Override
    public AuthResponse refreshToken(String oldToken) {
        if (oldToken == null || oldToken.isBlank()) {
            throw new BusinessValidationException("Invalid or expired token", "INVALID_TOKEN");
        }
        if (oldToken.startsWith("Bearer ")) {
            oldToken = oldToken.substring(7);
        }
        if (!jwtTokenProvider.isTokenValid(oldToken)) {
            throw new BusinessValidationException("Invalid or expired token", "INVALID_TOKEN");
        }
        io.jsonwebtoken.Claims claims = jwtTokenProvider.extractClaims(oldToken);
        ShopUserPrincipal principal = new ShopUserPrincipal(
                claims.get("userId", Long.class),
                claims.get("shopId", Long.class),
                claims.getSubject(),
                "",
                UserRole.valueOf(claims.get("role", String.class))
        );
        String newToken = jwtTokenProvider.createToken(principal);
        return AuthResponse.builder()
                .shopId(principal.getShopId())
                .userId(principal.getUserId())
                .token(newToken)
                .refreshToken(null)
                .build();
    }
}
