package com.hisaab_khata.hisaab_khata.auth;


import com.hisaab_khata.hisaab_khata.domain.User;
import com.hisaab_khata.hisaab_khata.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final UserRepository userRepo;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {

        String mobile = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        User user = userRepo.findByMobile(mobile)
                .orElseThrow(() -> new BadCredentialsException("Invalid mobile or password"));

        if (!user.getActive()) {
            throw new DisabledException("User account is disabled");
        }

        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid mobile or password");
        }

        ShopUserPrincipal principal = ShopUserPrincipal.builder()
                .userId(user.getId())
                .shopId(user.getShopId())
                .mobile(user.getMobile())
                .passwordHash(user.getPasswordHash())
                .role(user.getRole())
                .build();

        return new UsernamePasswordAuthenticationToken(
                principal, null, principal.getAuthorities()
        );
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.equals(authentication);
    }
}

