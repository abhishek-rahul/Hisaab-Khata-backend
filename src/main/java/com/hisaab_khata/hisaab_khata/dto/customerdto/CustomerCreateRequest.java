package com.hisaab_khata.hisaab_khata.dto.customerdto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerCreateRequest {
    private String name;
    private String phone;
}

