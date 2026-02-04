package com.hisaab_khata.hisaab_khata.service;



import com.hisaab_khata.hisaab_khata.dto.saledto.SaleCreateRequest;
import com.hisaab_khata.hisaab_khata.dto.saledto.SaleResponse;

import java.util.List;

public interface ISaleService {

    SaleResponse createSale(SaleCreateRequest request);

    SaleResponse getSale(Long id);

    List<SaleResponse> getAllSales();

}

