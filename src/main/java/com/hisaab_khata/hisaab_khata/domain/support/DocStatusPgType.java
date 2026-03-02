package com.hisaab_khata.hisaab_khata.domain.support;

import com.hisaab_khata.hisaab_khata.enums.DocStatus;

public class DocStatusPgType extends PostgreSQLEnumType<DocStatus> {

    public DocStatusPgType() {
        super(DocStatus.class, "doc_status");
    }
}
