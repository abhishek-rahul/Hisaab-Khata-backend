package com.hisaab_khata.hisaab_khata.domain.support;

import com.hisaab_khata.hisaab_khata.enums.PurchaseUploadStatus;

public class PurchaseUploadStatusPgType extends PostgreSQLEnumType<PurchaseUploadStatus> {

    public PurchaseUploadStatusPgType() {
        super(PurchaseUploadStatus.class, "purchase_upload_status");
    }
}
