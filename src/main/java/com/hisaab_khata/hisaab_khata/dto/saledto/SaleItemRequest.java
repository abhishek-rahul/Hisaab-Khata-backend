package com.hisaab_khata.hisaab_khata.dto.saledto;


import jakarta.validation.constraints.*;
import lombok.*;

//import java.math.BigDecimal;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaleItemRequest {

    @NotNull(message = "productId is required")
    private Long productId;

    @NotNull(message = "quantity is required")
    @DecimalMin(value = "0.001", message = "Quantity must be greater than 0")
    private Double quantity;

    @NotBlank(message = "unit is required")
    private String unit;

    @NotNull(message = "sellingPrice is required")
    @DecimalMin(value = "0.01", message = "Price must be positive")
    private Double sellingPrice;
}

