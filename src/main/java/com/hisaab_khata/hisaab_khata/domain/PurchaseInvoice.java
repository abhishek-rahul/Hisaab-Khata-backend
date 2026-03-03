package com.hisaab_khata.hisaab_khata.domain;

import com.hisaab_khata.hisaab_khata.domain.support.DocStatusPgType;
import com.hisaab_khata.hisaab_khata.enums.DocStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "purchase_invoice")
public class PurchaseInvoice extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne(optional = false)
    @JoinColumn(name = "purchase_upload_id", nullable = false)
    private PurchaseUpload purchaseUpload;

    @ManyToOne
    @JoinColumn(name = "supplier_party_id")
    private Party supplierParty;

    @Column(name = "invoice_no")
    private String invoiceNo;

    @Column(name = "invoice_date")
    private LocalDate invoiceDate;

    @Column(name = "notes")
    private String notes;

    @Type(DocStatusPgType.class)
    @Column(name = "status", nullable = false)
    private DocStatus status;

    @Column(name = "version", nullable = false)
    private Integer version;

    @Column(name = "total_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "posted_at")
    private LocalDateTime postedAt;

    @OneToMany(mappedBy = "purchaseInvoice", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseInvoiceLine> lines = new ArrayList<>();
}
