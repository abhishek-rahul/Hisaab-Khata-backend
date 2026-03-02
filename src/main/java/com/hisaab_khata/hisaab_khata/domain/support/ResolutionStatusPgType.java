package com.hisaab_khata.hisaab_khata.domain.support;

import com.hisaab_khata.hisaab_khata.enums.ResolutionStatus;

public class ResolutionStatusPgType extends PostgreSQLEnumType<ResolutionStatus> {

    public ResolutionStatusPgType() {
        super(ResolutionStatus.class, "resolution_status");
    }
}
