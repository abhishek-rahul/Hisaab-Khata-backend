package com.hisaab_khata.hisaab_khata.dto.productdto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter @Builder
@NoArgsConstructor @AllArgsConstructor
public class CategoryCreateRequest {
    @NotBlank(message = "name is required")
    private String name;
}

