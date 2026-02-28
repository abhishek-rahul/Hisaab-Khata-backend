package com.hisaab_khata.hisaab_khata.dto.authdto;


import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class RegisterRequest {
    private String name;
    private String phone;
    private String password;
    private String shopName;
    private String city;
}

