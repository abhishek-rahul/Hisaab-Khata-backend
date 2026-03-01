package com.hisaab_khata.hisaab_khata.domain;

import com.hisaab_khata.hisaab_khata.enums.PaymentMode;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "purchase")
public class Purchase extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_id")
    private Shop shop;

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne
    @JoinColumn(name = "shop_product_id")
    private ShopProduct shopProduct;

    @ManyToOne
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    private Double quantity;
    private String unit;

    private Double costPrice;

    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    public Purchase(Long purchaseId) {
        super();
    }
}

