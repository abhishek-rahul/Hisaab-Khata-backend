package com.hisaab_khata.hisaab_khata.dto.authdto;


import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {
    private String ownerName;
    private String shopName;
    private String mobile;
    private String password;
}

