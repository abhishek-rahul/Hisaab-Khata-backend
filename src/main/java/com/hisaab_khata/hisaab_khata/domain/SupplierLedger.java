package com.hisaab_khata.hisaab_khata.domain;


import com.hisaab_khata.hisaab_khata.enums.LedgerType;
import jakarta.persistence.*;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "supplier_ledger")
public class SupplierLedger extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    @Enumerated(EnumType.STRING)
    private LedgerType type; // UDHAR_LIYA or JAMA_KIYA

    private Double amount;

    @ManyToOne
    @JoinColumn(name = "purchase_id")
    private Purchase purchase;
}

