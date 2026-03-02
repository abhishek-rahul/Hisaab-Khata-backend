package com.hisaab_khata.hisaab_khata.domain;

import com.hisaab_khata.hisaab_khata.domain.support.ResolutionStatusPgType;
import com.hisaab_khata.hisaab_khata.enums.ResolutionStatus;
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
@Table(name = "purchase_invoice_line")
public class PurchaseInvoiceLine extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "purchase_invoice_id", nullable = false)
    private PurchaseInvoice purchaseInvoice;

    @Column(name = "line_no", nullable = false)
    private Integer lineNo;

    @Column(name = "raw_name", nullable = false)
    private String rawName;

    @Column(name = "normalized_name", nullable = false)
    private String normalizedName;

    @Column(name = "quantity", nullable = false, precision = 18, scale = 6)
    private BigDecimal quantity;

    @Column(name = "unit", nullable = false)
    private String unit;

    @Column(name = "unit_price", nullable = false, precision = 18, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "line_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal lineAmount;

    @Type(ResolutionStatusPgType.class)
    @Column(name = "resolution_status", nullable = false)
    private ResolutionStatus resolutionStatus;

    @ManyToOne
    @JoinColumn(name = "shop_product_id")
    private ShopProduct shopProduct;
}
