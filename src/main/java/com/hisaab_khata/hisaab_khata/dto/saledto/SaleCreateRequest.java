package com.hisaab_khata.hisaab_khata.dto.saledto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.List;


@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaleCreateRequest {

    private Long customerId;  // optional (required only for udhar)

    @NotEmpty(message = "Sale must include at least one item")
    private List<SaleItemRequest> items;

    // payment split
    @NotNull(message = "cashPaid is required")
    private Double cashPaid;

    @NotNull(message = "upiPaid is required")
    private Double upiPaid;

    @NotNull(message = "udharAmount is required")
    private Double udharAmount;

    // optional round off (±)
    @NotNull(message = "roundOff is required")
    private Double roundOff;
}


