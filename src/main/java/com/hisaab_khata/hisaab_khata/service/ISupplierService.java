package com.hisaab_khata.hisaab_khata.service;



import com.hisaab_khata.hisaab_khata.dto.supplierdto.SupplierCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.supplierdto.SupplierResponse;

import java.util.List;

public interface ISupplierService {

    SupplierResponse createSupplier(SupplierCreateRequest request);

    List<SupplierResponse> getAllSuppliers();


}

