package com.hisaab_khata.hisaab_khata.dto.purchasedraft;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hisaab_khata.hisaab_khata.enums.BaseUnit;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResolveLineRequest {
    @NotNull(message = "action is required")
    private ResolveAction action;
    private Long shopProductId;       // for CHOOSE_EXISTING
    private String canonicalName;     // for CREATE_NEW_PRODUCT
    private BaseUnit baseUnit;        // for CREATE_NEW_PRODUCT
    private Long categoryId;          // for CREATE_NEW_PRODUCT

    public enum ResolveAction {
        ACCEPT_SUGGESTION,
        CHOOSE_EXISTING,
        CREATE_NEW_PRODUCT
    }
}
