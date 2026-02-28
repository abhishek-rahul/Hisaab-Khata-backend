package com.hisaab_khata.hisaab_khata.service.impl;

import com.hisaab_khata.hisaab_khata.domain.RefreshToken;
import com.hisaab_khata.hisaab_khata.exception.UnauthorizedException;
import com.hisaab_khata.hisaab_khata.repository.RefreshTokenRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository repo;

    @Transactional
    public RefreshToken create(Long userId) {
        repo.deleteByUserId(userId);

        RefreshToken token = RefreshToken.builder()
                .userId(userId)
                .token(UUID.randomUUID().toString())
                .expiry(OffsetDateTime.now().plusDays(30))
                .revoked(false)
                .createdAt(OffsetDateTime.now())
                .build();

        return repo.save(token);
    }

    public RefreshToken validate(String token) {
        RefreshToken ref = repo.findByToken(token)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token", "INVALID_REFRESH_TOKEN"));
        if (Boolean.TRUE.equals(ref.getRevoked())) {
            throw new UnauthorizedException("Refresh token revoked", "REFRESH_TOKEN_REVOKED");
        }
        if (ref.getExpiry().isBefore(OffsetDateTime.now())) {
            throw new UnauthorizedException("Refresh token expired", "REFRESH_TOKEN_EXPIRED");
        }
        return ref;
    }
}

