package com.hisaab_khata.hisaab_khata.dto.partydto;

import com.hisaab_khata.hisaab_khata.enums.PartyType;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PartyResponse {

    private Long id;
    private String name;
    private String phone;
    private PartyType type;
}
