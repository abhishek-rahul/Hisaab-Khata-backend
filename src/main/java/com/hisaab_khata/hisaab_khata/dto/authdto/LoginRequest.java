package com.hisaab_khata.hisaab_khata.dto.authdto;



import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class LoginRequest {
    private String phone;
    private String password;
}


