package com.hisaab_khata.hisaab_khata.domain;

import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne(optional = false)
    @JoinColumn(name = "category_id")
    private Category category;

    private String name;

    private String unit;      // e.g., KG, Litre, Box
    private String baseUnit;  // e.g., GRAM, ML, PCS

    private Integer conversion;  // e.g., 1 KG = 1000 GRAM

    private Double defaultSalePrice;

    private Double minStock;

    // optional fallback cost if no purchase present
    private Double openingCostPrice;

    public Double convertToBase(Double qty) {
        return qty*conversion; // conversion stored as Double
    }
}

