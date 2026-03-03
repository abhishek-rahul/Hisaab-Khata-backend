package com.hisaab_khata.hisaab_khata.domain.support;

import com.hisaab_khata.hisaab_khata.enums.MappingSource;

public class MappingSourcePgType extends PostgreSQLEnumType<MappingSource> {

    public MappingSourcePgType() {
        super(MappingSource.class, "mapping_source");
    }
}
