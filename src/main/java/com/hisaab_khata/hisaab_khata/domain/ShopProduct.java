package com.hisaab_khata.hisaab_khata.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "shop_product")
public class ShopProduct extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(optional = false)
    @JoinColumn(name = "master_product_id", nullable = false)
    private MasterProduct masterProduct;

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;

    @Column(name = "display_name", nullable = false)
    private String displayName;

    @Column(name = "display_unit", nullable = false)
    private String displayUnit;

    @Column(name = "conversion_to_base", nullable = false, precision = 18, scale = 6)
    private BigDecimal conversionToBase;

    @Column(name = "selling_price", precision = 18, scale = 2)
    private BigDecimal sellingPrice;

    @Column(name = "min_stock_in_base", nullable = false, precision = 18, scale = 6)
    private BigDecimal minStockInBase;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive;
}
