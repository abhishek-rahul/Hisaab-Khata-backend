package com.hisaab_khata.hisaab_khata.domain;

import com.hisaab_khata.hisaab_khata.domain.support.MappingSourcePgType;
import com.hisaab_khata.hisaab_khata.enums.MappingSource;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "supplier_product_mapping")
public class SupplierProductMapping extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(optional = false)
    @JoinColumn(name = "supplier_party_id", nullable = false)
    private Party supplierParty;

    @Column(name = "normalized_name", nullable = false)
    private String normalizedName;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_product_id", nullable = false)
    private ShopProduct shopProduct;

    @Type(MappingSourcePgType.class)
    @Column(name = "source", nullable = false)
    private MappingSource source;

    @Column(name = "confidence_score", precision = 5, scale = 4)
    private BigDecimal confidenceScore;
}
