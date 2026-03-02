package com.hisaab_khata.hisaab_khata.domain.support;

import com.hisaab_khata.hisaab_khata.enums.PurchaseUploadDocKind;

public class PurchaseUploadDocKindPgType extends PostgreSQLEnumType<PurchaseUploadDocKind> {

    public PurchaseUploadDocKindPgType() {
        super(PurchaseUploadDocKind.class, "purchase_upload_doc_kind");
    }
}
