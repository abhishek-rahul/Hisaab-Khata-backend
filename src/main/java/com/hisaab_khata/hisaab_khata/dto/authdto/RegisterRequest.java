package com.hisaab_khata.hisaab_khata.dto.authdto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "Shop name is required")
    private String shopName;

    @NotBlank(message = "Owner name is required")
    private String ownerName;

    @NotBlank(message = "Mobile is required")
    private String mobile;

    @NotBlank(message = "Password is required")
    private String password;
}

