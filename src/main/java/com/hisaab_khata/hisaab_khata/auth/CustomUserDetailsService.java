package com.hisaab_khata.hisaab_khata.auth;



import com.hisaab_khata.hisaab_khata.domain.User;
import com.hisaab_khata.hisaab_khata.repository.UserRepository;
import lombok.RequiredArgsConstructor;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;


import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepo;

    @Override
    public UserDetails loadUserByUsername(String mobile) throws UsernameNotFoundException {

        User user = userRepo.findByMobile(mobile)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return ShopUserPrincipal.builder()
                .userId(user.getId())
                .shopId(user.getShopId())
                .mobile(user.getMobile())
                .passwordHash(user.getPasswordHash())
                .role(user.getRole())
                .build();
    }
}


