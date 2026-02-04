package com.hisaab_khata.hisaab_khata.dto.authdto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class RefreshTokenResponse {
    private String token;
}

