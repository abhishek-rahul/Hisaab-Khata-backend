package com.hisaab_khata.hisaab_khata.dto.authdto;



import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class AuthResponse {
    private Long shopId;
    private Long userId;
    private String token;
    private String refreshToken;
}

