package com.hisaab_khata.hisaab_khata.auth;


import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.*;
import org.springframework.security.authentication.*;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.*;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtFilter;
    private final CustomAuthenticationProvider customAuthenticationProvider;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final TenantFilter tenantFilter;

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http.csrf(csrf -> csrf.disable())
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .authenticationProvider(customAuthenticationProvider)
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("ROLE_ADMIN")
                        .requestMatchers("/staff/**").hasAnyAuthority("ROLE_STAFF", "ROLE_ADMIN")
                        .requestMatchers("/owner/**").hasAnyAuthority("ROLE_OWNER", "ROLE_ADMIN")
                        .requestMatchers("/purchase/**").hasAnyAuthority("ROLE_OWNER", "ROLE_ADMIN")
                        .requestMatchers("/category/**").hasAnyAuthority("ROLE_OWNER", "ROLE_ADMIN")
                        .requestMatchers("/product/**").hasAnyAuthority("ROLE_OWNER", "ROLE_ADMIN")
                        .requestMatchers("/supplier/**").hasAnyAuthority("ROLE_OWNER", "ROLE_ADMIN")
                        .anyRequest().authenticated()
                )
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(tenantFilter, JwtAuthenticationFilter.class);

        return http.build();
    }
}


