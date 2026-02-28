package com.hisaab_khata.hisaab_khata.auth;

import com.hisaab_khata.hisaab_khata.domain.AppUser;
import com.hisaab_khata.hisaab_khata.domain.AuthCredential;
import com.hisaab_khata.hisaab_khata.domain.ShopUser;
import com.hisaab_khata.hisaab_khata.enums.UserStatus;
import com.hisaab_khata.hisaab_khata.repository.AppUserRepository;
import com.hisaab_khata.hisaab_khata.repository.AuthCredentialRepository;
import com.hisaab_khata.hisaab_khata.repository.ShopUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;
    private final AuthCredentialRepository authCredentialRepository;
    private final ShopUserRepository shopUserRepository;

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        AppUser appUser = appUserRepository.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        AuthCredential credential = authCredentialRepository.findById(appUser.getId())
                .orElseThrow(() -> new UsernameNotFoundException("Credentials not found"));

        List<ShopUser> memberships = shopUserRepository.findByUserIdAndStatus(appUser.getId(), UserStatus.ACTIVE);
        if (memberships.isEmpty()) {
            throw new UsernameNotFoundException("No active shop for user");
        }
        ShopUser shopUser = memberships.get(0);

        return ShopUserPrincipal.builder()
                .userId(appUser.getId())
                .shopId(shopUser.getShopId())
                .mobile(appUser.getPhone())
                .passwordHash(credential.getPasswordHash())
                .role(shopUser.getRole())
                .build();
    }
}
