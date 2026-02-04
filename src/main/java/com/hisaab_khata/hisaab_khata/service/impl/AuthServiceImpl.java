package com.hisaab_khata.hisaab_khata.service.impl;


import com.hisaab_khata.hisaab_khata.auth.JwtUtil;
import com.hisaab_khata.hisaab_khata.auth.ShopUserPrincipal;
import com.hisaab_khata.hisaab_khata.domain.RefreshToken;
import com.hisaab_khata.hisaab_khata.domain.Shop;
import com.hisaab_khata.hisaab_khata.domain.User;
import com.hisaab_khata.hisaab_khata.dto.authdto.AuthResponse;
import com.hisaab_khata.hisaab_khata.dto.authdto.LoginRequest;
import com.hisaab_khata.hisaab_khata.dto.authdto.RegisterRequest;
import com.hisaab_khata.hisaab_khata.enums.UserRole;
import com.hisaab_khata.hisaab_khata.exception.BusinessValidationException;
import com.hisaab_khata.hisaab_khata.exception.ResourceNotFoundException;
import com.hisaab_khata.hisaab_khata.repository.ShopRepository;
import com.hisaab_khata.hisaab_khata.repository.UserRepository;
import com.hisaab_khata.hisaab_khata.service.IAuthService;
import io.jsonwebtoken.Claims;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements IAuthService {

    @Autowired
    private final ShopRepository shopRepository;

    @Autowired
    private final UserRepository userRepository;

    @Autowired
    private final JwtUtil jwtTokenProvider;

    @Autowired
    private final PasswordEncoder passwordEncoder;

    private final AuthenticationManager authManager;
    private final RefreshTokenService refreshTokenService;

    @Override
    public AuthResponse register(RegisterRequest req) {

        // Create shop
        Shop shop = Shop.builder()
                .shopName(req.getShopName())
                .phone(req.getMobile())
                .ownerName(req.getOwnerName())
                .build();

        shopRepository.save(shop);

        // Create user
        User user = User.builder()
                .name(req.getOwnerName())
                .mobile(req.getMobile())
                .passwordHash(passwordEncoder.encode(req.getPassword()))
                .shopId(shop.getId())
                .role(UserRole.OWNER)
                .active(true)
                .build();

        userRepository.save(user);

        ShopUserPrincipal principal = new ShopUserPrincipal(
                user.getId(), shop.getId(), user.getMobile(),
                user.getPasswordHash(), user.getRole()
        );

        String token = jwtTokenProvider.createToken(principal);
        RefreshToken ref = refreshTokenService.create(user.getId());

        return AuthResponse.builder()
                .shopId(shop.getId())
                .userId(user.getId())
                .token(token)
                .refreshToken(ref.getToken())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest req) {

        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(req.getMobile(), req.getPassword())
        );

        User user = userRepository.findByMobile(req.getMobile())
                .orElseThrow(() -> new ResourceNotFoundException("User not found", "USER_NOT_FOUND"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new BusinessValidationException("Invalid password", "INVALID_PASSWORD");
        }

        ShopUserPrincipal principal = new ShopUserPrincipal(
                user.getId(), user.getShopId(), user.getMobile(),
                user.getPasswordHash(), user.getRole()
        );

        String token = jwtTokenProvider.createToken(principal);
        RefreshToken ref = refreshTokenService.create(principal.getUserId());

        return AuthResponse.builder()
                .shopId(principal.getShopId())
                .userId(principal.getUserId())
                .token(token)
                .refreshToken(ref.getToken())
                .build();
    }

    @Override
    public AuthResponse refreshToken(String refreshToken) {

        RefreshToken ref = refreshTokenService.validate(refreshToken);

        User user = userRepository.findById(ref.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        ShopUserPrincipal principal = ShopUserPrincipal.builder()
                .userId(user.getId())
                .shopId(user.getShopId())
                .mobile(user.getMobile())
                .passwordHash(user.getPasswordHash())
                .role(user.getRole())
                .build();

        String newToken = jwtTokenProvider.createToken(principal);

        return AuthResponse.builder()
                .shopId(principal.getShopId())
                .userId(principal.getUserId())
                .token(newToken)
                .refreshToken(refreshToken)
                .build();
    }
}

