package com.hisaab_khata.hisaab_khata.domain;

import com.hisaab_khata.hisaab_khata.enums.LedgerEntryType;
import com.hisaab_khata.hisaab_khata.enums.LedgerReferenceType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Append-only ledger entries for a party. Phase 2: no inserts from app; used for read in later phases.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "party_ledger")
public class PartyLedger {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "shop_id", nullable = false)
    private Long shopId;

    @ManyToOne(optional = false)
    @JoinColumn(name = "party_id", nullable = false)
    private Party party;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "entry_type", nullable = false)
    private LedgerEntryType entryType;

    @Column(name = "dr_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal drAmount;

    @Column(name = "cr_amount", nullable = false, precision = 18, scale = 2)
    private BigDecimal crAmount;

    @JdbcTypeCode(SqlTypes.NAMED_ENUM)
    @Column(name = "reference_type")
    private LedgerReferenceType referenceType;

    @Column(name = "reference_id")
    private Long referenceId;

    private String remarks;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
