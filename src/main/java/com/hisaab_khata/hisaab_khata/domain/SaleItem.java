package com.hisaab_khata.hisaab_khata.domain;



import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "sale_item")
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SaleItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // MULTI-TENANT COLUMN
    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // quantity entered by user (PCS / KG etc.)
    @Column(name = "quantity", nullable = false)
    private Double quantity;

    // converted to base unit (GRAM / PCS / ML)
    @Column(name = "quantity_base", nullable = false)
    private Double quantityBase;

    @Column(name = "unit", nullable = false, length = 20)
    private String unit;

    @Column(name = "selling_price", nullable = false)
    private Double sellingPrice;

    @Column(name = "total_price", nullable = false)
    private Double totalPrice;

    // final profit = (sellingPrice - costPrice) * quantity
    @Column(name = "profit")
    private Double profit;
}

