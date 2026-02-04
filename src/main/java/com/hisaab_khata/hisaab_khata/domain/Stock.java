package com.hisaab_khata.hisaab_khata.domain;


import jakarta.persistence.*;
import lombok.*;

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

    // MULTI-TENANCY REQUIRED
    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @OneToOne(optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    // Always stored in BASE UNIT
    @Column(nullable = false)
    private Double quantity;
}
