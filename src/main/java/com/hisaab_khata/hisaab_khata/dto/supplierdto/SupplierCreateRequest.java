package com.hisaab_khata.hisaab_khata.dto.supplierdto;

import lombok.*;

@Getter @Setter @Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupplierCreateRequest {
    private String name;
    private String phone;
}

