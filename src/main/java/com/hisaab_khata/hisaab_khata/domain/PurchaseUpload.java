package com.hisaab_khata.hisaab_khata.domain;

import com.hisaab_khata.hisaab_khata.domain.support.PurchaseUploadDocKindPgType;
import com.hisaab_khata.hisaab_khata.domain.support.PurchaseUploadStatusPgType;
import com.hisaab_khata.hisaab_khata.enums.PurchaseUploadDocKind;
import com.hisaab_khata.hisaab_khata.enums.PurchaseUploadStatus;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "purchase_upload")
public class PurchaseUpload extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "shop_id", nullable = false)
    private Shop shop;

    @ManyToOne
    @JoinColumn(name = "supplier_party_id")
    private Party supplierParty;

    @Type(PurchaseUploadDocKindPgType.class)
    @Column(name = "doc_kind", nullable = false)
    private PurchaseUploadDocKind docKind;

    @Column(name = "parser_key", nullable = false)
    private String parserKey;

    @Type(PurchaseUploadStatusPgType.class)
    @Column(name = "status", nullable = false)
    private PurchaseUploadStatus status;

    @Column(name = "original_file_name")
    private String originalFileName;

    @OneToMany(mappedBy = "purchaseUpload", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PurchaseUploadLine> lines = new ArrayList<>();
}
