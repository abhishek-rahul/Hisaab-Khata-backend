package com.hisaab_khata.hisaab_khata.dto.authdto;

import com.hisaab_khata.hisaab_khata.enums.UserRole;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffResponse {
    private Long id;
    private Long shopId;
    private String name;
    private String mobile;
    private UserRole role;
    private Boolean active;
}
