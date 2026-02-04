package com.hisaab_khata.hisaab_khata.mapper;


import com.hisaab_khata.hisaab_khata.domain.CustomerLedger;
import com.hisaab_khata.hisaab_khata.dto.customerdto.CustomerLedgerEntryResponse;
import com.hisaab_khata.hisaab_khata.mapper.config.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface CustomerLedgerMapper {

    @Mapping(target = "date", source = "createdAt", dateFormat = "yyyy-MM-dd")
    CustomerLedgerEntryResponse toEntry(CustomerLedger ledger);
}
