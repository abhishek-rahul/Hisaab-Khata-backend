package com.hisaab_khata.hisaab_khata.auth;

import com.hisaab_khata.hisaab_khata.domain.AppUser;
import com.hisaab_khata.hisaab_khata.domain.AuthCredential;
import com.hisaab_khata.hisaab_khata.domain.ShopUser;
import com.hisaab_khata.hisaab_khata.enums.UserStatus;
import com.hisaab_khata.hisaab_khata.repository.AppUserRepository;
import com.hisaab_khata.hisaab_khata.repository.AuthCredentialRepository;
import com.hisaab_khata.hisaab_khata.repository.ShopUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class CustomAuthenticationProvider implements AuthenticationProvider {

    private final AppUserRepository appUserRepository;
    private final AuthCredentialRepository authCredentialRepository;
    private final ShopUserRepository shopUserRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        String phone = authentication.getName();
        String rawPassword = authentication.getCredentials().toString();

        AppUser appUser = appUserRepository.findByPhone(phone)
                .orElseThrow(() -> new BadCredentialsException("Invalid phone or password"));

        AuthCredential credential = authCredentialRepository.findById(appUser.getId())
                .orElseThrow(() -> new BadCredentialsException("Invalid phone or password"));

        if (!passwordEncoder.matches(rawPassword, credential.getPasswordHash())) {
            throw new BadCredentialsException("Invalid phone or password");
        }

        List<ShopUser> memberships = shopUserRepository.findByUserIdAndStatus(appUser.getId(), UserStatus.ACTIVE);
        if (memberships.isEmpty()) {
            throw new DisabledException("No active shop for user");
        }
        ShopUser shopUser = memberships.get(0);

        ShopUserPrincipal principal = ShopUserPrincipal.builder()
                .userId(appUser.getId())
                .shopId(shopUser.getShopId())
                .mobile(appUser.getPhone())
                .passwordHash(credential.getPasswordHash())
                .role(shopUser.getRole())
                .build();

        return new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.equals(authentication);
    }
}
