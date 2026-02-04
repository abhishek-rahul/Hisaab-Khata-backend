package com.hisaab_khata.hisaab_khata.dto.authdto;


import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class RegisterResponse {
    private Long shopId;
    private String token;
    private String refreshToken;
}

