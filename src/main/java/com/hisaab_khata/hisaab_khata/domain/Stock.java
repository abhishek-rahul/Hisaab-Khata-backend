package com.hisaab_khata.hisaab_khata.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * Phase 3: Stock snapshot per shop_product. Quantity in base unit; negative allowed.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "stock")
public class Stock extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "shop_product_id", nullable = false, unique = true)
    private ShopProduct shopProduct;

    @Column(nullable = false, precision = 18, scale = 6)
    private BigDecimal quantity;
}
