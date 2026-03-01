package com.hisaab_khata.hisaab_khata.dto.partydto;

import com.hisaab_khata.hisaab_khata.enums.PartyType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePartyRequest {

    @NotBlank(message = "name is required")
    private String name;

    private String phone;

    @NotNull(message = "type is required")
    private PartyType type;
}
