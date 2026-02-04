package com.hisaab_khata.hisaab_khata.mapper;


import com.hisaab_khata.hisaab_khata.domain.Supplier;
import com.hisaab_khata.hisaab_khata.domain.SupplierLedger;
import com.hisaab_khata.hisaab_khata.dto.supplierdto.SupplierLedgerEntryResponse;
import com.hisaab_khata.hisaab_khata.dto.supplierdto.SupplierLedgerResponse;
import com.hisaab_khata.hisaab_khata.mapper.config.GlobalMapperConfig;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(config = GlobalMapperConfig.class)
public interface SupplierLedgerMapper {

    @Mapping(target = "date", source = "createdAt", dateFormat = "yyyy-MM-dd")
    SupplierLedgerEntryResponse toEntry(SupplierLedger ledger);

    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.name")
    SupplierLedgerResponse toSummary(Supplier supplier);
}

