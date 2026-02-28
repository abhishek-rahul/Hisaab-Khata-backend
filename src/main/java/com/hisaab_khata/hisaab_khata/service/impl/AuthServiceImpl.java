package com.hisaab_khata.hisaab_khata.service.impl;


import com.hisaab_khata.hisaab_khata.auth.JwtUtil;
import com.hisaab_khata.hisaab_khata.auth.ShopUserPrincipal;
import com.hisaab_khata.hisaab_khata.domain.RefreshToken;
import com.hisaab_khata.hisaab_khata.domain.Shop;
import com.hisaab_khata.hisaab_khata.domain.User;
import com.hisaab_khata.hisaab_khata.dto.authdto.AuthResponse;
import com.hisaab_khata.hisaab_khata.dto.authdto.LoginRequest;
import com.hisaab_khata.hisaab_khata.dto.authdto.RegisterRequest;
import com.hisaab_khata.hisaab_khata.dto.authdto.StaffCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.authdto.StaffResponse;
import com.hisaab_khata.hisaab_khata.enums.UserRole;
import com.hisaab_khata.hisaab_khata.enums.UserStatus;
import com.hisaab_khata.hisaab_khata.exception.ConflictException;
import com.hisaab_khata.hisaab_khata.repository.ShopRepository;
import com.hisaab_khata.hisaab_khata.repository.UserRepository;
import com.hisaab_khata.hisaab_khata.service.IAuthService;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
//import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthServiceImpl implements IAuthService {

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final JwtUtil jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authManager;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(ShopRepository shopRepository, UserRepository userRepository,
                           JwtUtil jwtTokenProvider, PasswordEncoder passwordEncoder,
                           AuthenticationManager authManager, RefreshTokenService refreshTokenService) {
        this.shopRepository = shopRepository;
        this.userRepository = userRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.passwordEncoder = passwordEncoder;
        this.authManager = authManager;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.findByMobile(req.getMobile()).isPresent()) {
            throw new ConflictException("Mobile already registered", "MOBILE_ALREADY_EXISTS");
        }
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
                .status(UserStatus.ACTIVE)
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
        ShopUserPrincipal principal = (ShopUserPrincipal) auth.getPrincipal();
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

        Optional<User> optUser = userRepository.findById(ref.getUserId());
        if (optUser.isEmpty()) {
            throw new RuntimeException("User not found");
        }
        User user = optUser.get();

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

    @Override
    public StaffResponse createStaff(Long shopId, StaffCreateRequest request) {
        if (userRepository.findByMobile(request.getMobile()).isPresent()) {
            throw new ConflictException("Mobile already registered", "MOBILE_ALREADY_EXISTS");
        }
        User staff = User.builder()
                .shopId(shopId)
                .name(request.getName())
                .mobile(request.getMobile())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(UserRole.STAFF)
                .status(UserStatus.ACTIVE)
                .active(true)
                .build();
        staff = userRepository.save(staff);
        return StaffResponse.builder()
                .id(staff.getId())
                .shopId(staff.getShopId())
                .name(staff.getName())
                .mobile(staff.getMobile())
                .role(staff.getRole())
                .active(staff.getActive())
                .build();
    }
}

